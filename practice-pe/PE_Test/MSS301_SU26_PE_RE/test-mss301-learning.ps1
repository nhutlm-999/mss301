# Run to test: powershell -ExecutionPolicy Bypass -File .\test-mss301-learning.ps1

param(
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$SkipNegativeTests
)

$ErrorActionPreference = "Stop"
$script:Passed = 0
$script:Failed = 0
$script:Results = @()
$script:CourseId = $null
$script:DraftCourseId = $null
$script:Course2Id = $null
$script:DefaultCourseId = $null
$script:EnrollmentId = $null
$script:Enrollment2Id = $null

function Write-TestResult {
    param([string]$Name, [bool]$Success, [string]$Detail = "")
    if ($Success) {
        $script:Passed++
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $state = "PASS"
    } else {
        $script:Failed++
        Write-Host "[FAIL] $Name $Detail" -ForegroundColor Red
        $state = "FAIL"
    }
    $script:Results += [pscustomobject]@{
        Result = $state
        Test   = $Name
        Detail = $Detail
    }
}

function Invoke-Api {
    param(
        [ValidateSet("GET","POST","PUT","DELETE")]
        [string]$Method,
        [string]$Path,
        [object]$Body = $null
    )

    $uri = "$($BaseUrl.TrimEnd('/'))$Path"
    $params = @{
        Uri             = $uri
        Method          = $Method
        TimeoutSec      = 20
        UseBasicParsing = $true
        Headers         = @{ Accept = "application/json" }
    }

    if ($null -ne $Body) {
        $params.ContentType = "application/json; charset=utf-8"
        $params.Body = ($Body | ConvertTo-Json -Depth 20 -Compress)
    }

    try {
        $response = Invoke-WebRequest @params
        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($response.Content)) {
            try { $json = $response.Content | ConvertFrom-Json } catch {}
        }

        return [pscustomobject]@{
            HttpStatus = [int]$response.StatusCode
            Json       = $json
            Raw        = $response.Content
        }
    }
    catch {
        $status = 0
        $raw = ""
        if ($_.Exception.Response) {
            try { $status = [int]$_.Exception.Response.StatusCode } catch {}
            # Windows PowerShell 5.1
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                if ($stream) {
                    $reader = New-Object System.IO.StreamReader($stream)
                    $raw = $reader.ReadToEnd()
                    $reader.Dispose()
                }
            } catch {}
        }
        # PowerShell 7+
        if ([string]::IsNullOrWhiteSpace($raw) -and $_.ErrorDetails -and $_.ErrorDetails.Message) {
            $raw = $_.ErrorDetails.Message
        }

        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($raw)) {
            try { $json = $raw | ConvertFrom-Json } catch {}
        }

        return [pscustomobject]@{
            HttpStatus = $status
            Json       = $json
            Raw        = $raw
        }
    }
}

# NOTE: parameter order = Name, Method, Path, ExpectedHttp, Body, ExpectedApiStatus
function Test-Api {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Path,
        [int]$ExpectedHttp,
        [object]$Body = $null,
        [int]$ExpectedApiStatus = -1
    )

    $r = Invoke-Api -Method $Method -Path $Path -Body $Body
    $ok = ($r.HttpStatus -eq $ExpectedHttp)

    if ($ok -and $ExpectedApiStatus -ge 0) {
        $ok = ($null -ne $r.Json -and [int]$r.Json.status -eq $ExpectedApiStatus)
    }

    $detail = "expected HTTP=$ExpectedHttp, got HTTP=$($r.HttpStatus), API status=$($r.Json.status)"
    Write-TestResult -Name $Name -Success $ok -Detail $detail
    return $r
}

function Test-Condition {
    param([string]$Name, [bool]$Condition, [string]$Detail = "")
    Write-TestResult -Name $Name -Success $Condition -Detail $Detail
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host " MSS301 FULL API TEST - COURSE + ENROLLMENT" -ForegroundColor Cyan
Write-Host " Gateway: $BaseUrl" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# 0. Gateway connectivity
$healthCheck = Invoke-Api -Method GET -Path "/api/courses?page=0&size=1"
if ($healthCheck.HttpStatus -ne 200) {
    Write-Host "Cannot reach the API correctly at $BaseUrl (HTTP $($healthCheck.HttpStatus))." -ForegroundColor Red
    Write-Host "Start SQL Server, Course Service (8081), Enrollment Service (8082), then Gateway (8080)." -ForegroundColor Yellow
    exit 1
}
Test-Condition "Gateway and Course Service reachable" $true

# Unique test values (courseCode must be alphanumeric and <= 20 chars)
$suffix        = (Get-Date).ToString("MMddHHmmss")
$courseCode    = "T$suffix"
$draftCode     = "D$suffix"
$defaultCode   = "Z$suffix"
$course2Code   = "S$suffix"
$startDate     = (Get-Date).Date.AddDays(2).ToString("yyyy-MM-dd")
$endDate       = (Get-Date).Date.AddDays(90).ToString("yyyy-MM-dd")
$endDateNew    = (Get-Date).Date.AddDays(120).ToString("yyyy-MM-dd")
$pastDate      = (Get-Date).Date.AddDays(-1).ToString("yyyy-MM-dd")
$enrollDate    = (Get-Date).Date.ToString("yyyy-MM-dd")

$openCourseBody = @{
    courseCode   = $courseCode
    title        = "Gateway Test Course $suffix"
    description  = "Created by automated PowerShell test"
    credits      = 3
    status       = "OPEN"
    startDate    = $startDate
    endDate      = $endDate
    instructorId = 1001
}

$draftCourseBody = @{
    courseCode   = $draftCode
    title        = "Gateway Draft Course $suffix"
    description  = "Draft course for negative enrollment test"
    credits      = 2
    status       = "DRAFT"
    startDate    = $startDate
    endDate      = $endDate
    instructorId = 1002
}

# ==================== GATEWAY DOCS ====================
Test-Api "Gateway serves Course Service OpenAPI docs" "GET" "/course/v3/api-docs" 200 | Out-Null
Test-Api "Gateway serves Enrollment Service OpenAPI docs" "GET" "/enrollment/v3/api-docs" 200 | Out-Null

# ==================== COURSE ====================

# 1. COURSE - CREATE
$r = Test-Api "Create OPEN course" "POST" "/api/courses" 201 $openCourseBody 1
if ($r.Json -and $r.Json.data) { $script:CourseId = [long]$r.Json.data.courseId }
Test-Condition "Created course returns courseId" ($null -ne $script:CourseId)
Test-Condition "Created course keeps status OPEN" ($r.Json.data.status -eq "OPEN")

$r = Test-Api "Create DRAFT course" "POST" "/api/courses" 201 $draftCourseBody 1
if ($r.Json -and $r.Json.data) { $script:DraftCourseId = [long]$r.Json.data.courseId }
Test-Condition "Created DRAFT course keeps status DRAFT" ($r.Json.data.status -eq "DRAFT")

$defaultBody = $openCourseBody.Clone()
$defaultBody.courseCode = $defaultCode
$defaultBody.title = "Gateway Default Course $suffix"
$defaultBody.Remove("status")
$r = Test-Api "Create course without status" "POST" "/api/courses" 201 $defaultBody 1
if ($r.Json -and $r.Json.data) { $script:DefaultCourseId = [long]$r.Json.data.courseId }
Test-Condition "Course without status defaults to DRAFT" ($r.Json.data.status -eq "DRAFT")

$course2Body = $openCourseBody.Clone()
$course2Body.courseCode = $course2Code
$course2Body.title = "Gateway Second Course $suffix"
$r = Test-Api "Create second OPEN course" "POST" "/api/courses" 201 $course2Body 1
if ($r.Json -and $r.Json.data) { $script:Course2Id = [long]$r.Json.data.courseId }

# 2. COURSE - GET DETAIL
if ($script:CourseId) {
    $r = Test-Api "Get course detail" "GET" "/api/courses/$($script:CourseId)" 200 $null 1
    Test-Condition "Course detail has OPEN Enum status" ($r.Json.data.status -eq "OPEN")
    Test-Condition "Course detail startDate format yyyy-MM-dd" ($r.Json.data.startDate -eq $startDate)
} else {
    Test-Condition "Get course detail" $false "Skipped: course was not created"
}

# 3. COURSE - LIST / FILTERS
$r = Test-Api "List courses with pagination" "GET" "/api/courses?page=0&size=10" 200 $null 1
Test-Condition "Course list contains content array" ($null -ne $r.Json.data.content)

$r = Test-Api "Filter courses by title" "GET" "/api/courses?page=0&size=10&title=$suffix" 200 $null 1
Test-Condition "Title filter returns the 4 test courses" (@($r.Json.data.content | Where-Object { $_.title -like "*$suffix*" }).Count -eq 4)

$r = Test-Api "Filter courses by Enum status OPEN" "GET" "/api/courses?page=0&size=100&status=OPEN" 200 $null 1
Test-Condition "Course status filter returns only OPEN records" (@($r.Json.data.content | Where-Object { $_.status -ne "OPEN" }).Count -eq 0)

$r = Test-Api "Filter courses by title and status DRAFT" "GET" "/api/courses?page=0&size=10&title=$suffix&status=DRAFT" 200 $null 1
Test-Condition "Title + status filter returns 2 DRAFT test courses" (@($r.Json.data.content | Where-Object { $_.status -eq "DRAFT" -and $_.title -like "*$suffix*" }).Count -eq 2)


# 4. COURSE - UPDATE PARTIAL
if ($script:CourseId) {
    $r = Test-Api "Update course title and credits" "PUT" "/api/courses/$($script:CourseId)" 200 @{
        title   = "Gateway Test Course Updated $suffix"
        credits = 4
    } 1
    Test-Condition "Course update persisted title" ($r.Json.data.title -eq "Gateway Test Course Updated $suffix")
    Test-Condition "Course update persisted credits" ($r.Json.data.credits -eq 4)

    $r = Test-Api "Update course description only" "PUT" "/api/courses/$($script:CourseId)" 200 @{
        description = "Updated description"
    } 1
    Test-Condition "Description updated and credits unchanged" ($r.Json.data.description -eq "Updated description" -and $r.Json.data.credits -eq 4)

    $r = Test-Api "Update course endDate" "PUT" "/api/courses/$($script:CourseId)" 200 @{
        endDate = $endDateNew
    } 1
    Test-Condition "Course endDate persisted" ($r.Json.data.endDate -eq $endDateNew)

}

# 5. COURSE - NEGATIVE TESTS
if (-not $SkipNegativeTests) {
    $duplicateBody = $openCourseBody.Clone()
    $duplicateBody.title = "Duplicate course $suffix"
    Test-Api "Reject duplicate courseCode (HTTP 226)" "POST" "/api/courses" 226 $duplicateBody 3 | Out-Null

    if ($script:Course2Id) {
        Test-Api "Reject update to duplicate courseCode (HTTP 226)" "PUT" "/api/courses/$($script:Course2Id)" 226 @{ courseCode = $courseCode } 3 | Out-Null
        Test-Api "Reject update endDate before startDate" "PUT" "/api/courses/$($script:Course2Id)" 406 @{ endDate = (Get-Date).Date.ToString("yyyy-MM-dd") } 2 | Out-Null
    }

    $b = $openCourseBody.Clone(); $b.courseCode = "BAD$suffix"; $b.credits = 0
    Test-Api "Reject course credits = 0" "POST" "/api/courses" 406 $b 2 | Out-Null

    $b = $openCourseBody.Clone(); $b.courseCode = "BIG$suffix"; $b.credits = 11

    $b = $openCourseBody.Clone(); $b.courseCode = "BAD CODE!"
    Test-Api "Reject courseCode with special characters" "POST" "/api/courses" 406 $b 2 | Out-Null

    $b = $openCourseBody.Clone(); $b.courseCode = "L" * 21

    $b = $openCourseBody.Clone(); $b.courseCode = "B$suffix"; $b.title = "   "
    Test-Api "Reject blank course title" "POST" "/api/courses" 406 $b 2 | Out-Null

    $b = $openCourseBody.Clone(); $b.courseCode = "M$suffix"; $b.Remove("startDate")
    Test-Api "Reject course without startDate" "POST" "/api/courses" 406 $b 2 | Out-Null

    $b = $openCourseBody.Clone(); $b.courseCode = "W$suffix"; $b.endDate = $startDate; $b.startDate = $endDate
    Test-Api "Reject course endDate before startDate" "POST" "/api/courses" 406 $b 2 | Out-Null

    $b = $openCourseBody.Clone(); $b.courseCode = "P$suffix"; $b.startDate = $pastDate
    Test-Api "Reject course startDate in the past" "POST" "/api/courses" 406 $b 2 | Out-Null

    $b = $openCourseBody.Clone(); $b.courseCode = "E$suffix"; $b.status = "NOT_A_STATUS"
    Test-Api "Reject invalid CourseStatus Enum" "POST" "/api/courses" 406 $b 2 | Out-Null

    $b = $openCourseBody.Clone(); $b.courseCode = "F$suffix"; $b.startDate = "not-a-date"
    Test-Api "Reject invalid course date format" "POST" "/api/courses" 406 $b 2 | Out-Null

    Test-Api "Reject invalid course pagination (page=-1)" "GET" "/api/courses?page=-1&size=10" 406 $null 2 | Out-Null
    Test-Api "Reject invalid course pagination (size=0)" "GET" "/api/courses?page=0&size=0" 406 $null 2 | Out-Null
    Test-Api "Reject invalid course status query" "GET" "/api/courses?status=UNKNOWN" 406 $null 2 | Out-Null
}

# ==================== ENROLLMENT ====================

# 6. ENROLLMENT - CREATE ON OPEN COURSE
if ($script:CourseId) {
    $enrollmentBody = @{
        studentId      = 190284
        studentName    = "PowerShell Test Student $suffix"
        studentEmail   = "test$suffix@example.com"
        enrollmentDate = $enrollDate
        grade          = "A"
        courseId       = $script:CourseId
    }

    $r = Test-Api "Create enrollment in OPEN course" "POST" "/api/enrollments" 201 $enrollmentBody 1
    if ($r.Json -and $r.Json.data) { $script:EnrollmentId = [long]$r.Json.data.enrollmentId }
    Test-Condition "Created enrollment returns enrollmentId" ($null -ne $script:EnrollmentId)
    Test-Condition "Created enrollment status defaults to ACTIVE" ($r.Json.data.status -eq "ACTIVE")
    Test-Condition "Created enrollment keeps grade A" ($r.Json.data.grade -eq "A")

    $second = $enrollmentBody.Clone()
    $second.studentId = 190285
    $second.studentName = "Second Student $suffix"
    $second.studentEmail = "second$suffix@example.com"
    $second.Remove("grade")
    $r = Test-Api "Create enrollment without grade" "POST" "/api/enrollments" 201 $second 1
    if ($r.Json -and $r.Json.data) { $script:Enrollment2Id = [long]$r.Json.data.enrollmentId }
} else {
    Test-Condition "Create enrollment in OPEN course" $false "Skipped: OPEN course was not created"
}

# 7. ENROLLMENT - DETAIL / FEIGN
if ($script:EnrollmentId) {
    $r = Test-Api "Get enrollment detail with Feign CourseDTO" "GET" "/api/enrollments/$($script:EnrollmentId)" 200 $null 1
    Test-Condition "Enrollment detail embeds course data" ($null -ne $r.Json.data.course)
    Test-Condition "Feign CourseDTO status deserialized as OPEN" ($r.Json.data.course.status -eq "OPEN")
    Test-Condition "Enrollment status is ACTIVE" ($r.Json.data.status -eq "ACTIVE")
}

# 8. ENROLLMENT - UPDATE
if ($script:EnrollmentId) {
    $r = Test-Api "Update enrollment grade" "PUT" "/api/enrollments/$($script:EnrollmentId)" 200 @{ grade = "B" } 1
    Test-Condition "Enrollment grade updated" ($r.Json.data.grade -eq "B")

    $r = Test-Api "Update enrollment status Enum" "PUT" "/api/enrollments/$($script:EnrollmentId)" 200 @{ status = "COMPLETED" } 1
    Test-Condition "Enrollment status updated to COMPLETED" ($r.Json.data.status -eq "COMPLETED")

    $r = Test-Api "Update enrollment student name and email" "PUT" "/api/enrollments/$($script:EnrollmentId)" 200 @{
        studentName  = "Renamed Student $suffix"
        studentEmail = "renamed$suffix@example.com"
    } 1
    Test-Condition "Enrollment student name and email updated" ($r.Json.data.studentName -eq "Renamed Student $suffix" -and $r.Json.data.studentEmail -eq "renamed$suffix@example.com")

    if ($script:Course2Id) {
        $r = Test-Api "Move enrollment to another OPEN course" "PUT" "/api/enrollments/$($script:EnrollmentId)" 200 @{ courseId = $script:Course2Id } 1
        Test-Condition "Enrollment courseId changed" ($r.Json.data.courseId -eq $script:Course2Id)
    }

    # Restore ACTIVE before cancellation test
    Test-Api "Restore enrollment status ACTIVE" "PUT" "/api/enrollments/$($script:EnrollmentId)" 200 @{ status = "ACTIVE" } 1 | Out-Null
}

# 9. ENROLLMENT - LIST / FILTERS
$r = Test-Api "List enrollments with pagination" "GET" "/api/enrollments?page=0&size=10" 200 $null 1
Test-Condition "Enrollment list contains content array" ($null -ne $r.Json.data.content)

$r = Test-Api "Filter enrollments by studentName" "GET" "/api/enrollments?page=0&size=10&studentName=$suffix" 200 $null 1
Test-Condition "Student name filter returns the 2 test enrollments" (@($r.Json.data.content | Where-Object { $_.studentName -like "*$suffix*" }).Count -eq 2)

$r = Test-Api "Filter enrollments by status ACTIVE" "GET" "/api/enrollments?page=0&size=100&status=ACTIVE" 200 $null 1
Test-Condition "Enrollment status filter returns only ACTIVE" (@($r.Json.data.content | Where-Object { $_.status -ne "ACTIVE" }).Count -eq 0)

if ($script:EnrollmentId) {
    $r = Test-Api "Filter enrollments by name and status" "GET" "/api/enrollments?page=0&size=10&studentName=$suffix&status=ACTIVE" 200 $null 1
    Test-Condition "Combined enrollment filter finds test record" (@($r.Json.data.content | Where-Object { $_.enrollmentId -eq $script:EnrollmentId }).Count -eq 1)
}

# 10. ENROLLMENT - NEGATIVE TESTS
if (-not $SkipNegativeTests) {
    if ($script:DraftCourseId) {
        $b = @{
            studentId = 190290; studentName = "Rejected Student $suffix"; studentEmail = "rejected$suffix@example.com"
            enrollmentDate = $enrollDate; courseId = $script:DraftCourseId
        }
        Test-Api "Reject enrollment in DRAFT course" "POST" "/api/enrollments" 400 $b 5 | Out-Null

        if ($script:EnrollmentId) {
            Test-Api "Reject moving enrollment to DRAFT course" "PUT" "/api/enrollments/$($script:EnrollmentId)" 400 @{ courseId = $script:DraftCourseId } 5 | Out-Null
        }
    }

    if ($script:CourseId) {
        $base = @{
            studentId = 190291; studentName = "Negative Student $suffix"; studentEmail = "neg$suffix@example.com"
            enrollmentDate = $enrollDate; grade = "A"; courseId = $script:CourseId
        }

        $b = $base.Clone(); $b.grade = "Z"
        Test-Api "Reject invalid enrollment grade" "POST" "/api/enrollments" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.status = "INVALID"
        Test-Api "Reject invalid EnrollmentStatus Enum" "POST" "/api/enrollments" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.studentEmail = "not-an-email"
        Test-Api "Reject invalid studentEmail" "POST" "/api/enrollments" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.studentId = 0
        Test-Api "Reject studentId = 0" "POST" "/api/enrollments" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.Remove("enrollmentDate")
        Test-Api "Reject enrollment without enrollmentDate" "POST" "/api/enrollments" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.courseId = 999999999
        Test-Api "Reject enrollment for non-existing course (404)" "POST" "/api/enrollments" 404 $b 4 | Out-Null

    }

    Test-Api "Reject invalid enrollment pagination" "GET" "/api/enrollments?page=-1&size=10" 406 $null 2 | Out-Null
    Test-Api "Reject invalid enrollment status query" "GET" "/api/enrollments?status=UNKNOWN" 406 $null 2 | Out-Null
}

# 11. CLOSE COURSE, THEN VERIFY CLOSED COURSE REJECTION
$closeId = $script:Course2Id
if ($closeId) {
    Test-Api "Close course" "DELETE" "/api/courses/$closeId" 200 $null 1 | Out-Null

    $r = Test-Api "Verify course status CLOSED" "GET" "/api/courses/$closeId" 200 $null 1
    Test-Condition "Course is CLOSED after DELETE" ($r.Json.data.status -eq "CLOSED")

    $closedBody = @{
        studentId = 190288; studentName = "Closed Course Student $suffix"; studentEmail = "closed$suffix@example.com"
        enrollmentDate = $enrollDate; courseId = $closeId
    }
    Test-Api "Reject enrollment in CLOSED course" "POST" "/api/enrollments" 400 $closedBody 5 | Out-Null

    if ($script:EnrollmentId) {
        $r = Test-Api "Enrollment detail still works after course CLOSED" "GET" "/api/enrollments/$($script:EnrollmentId)" 200 $null 1
        Test-Condition "Feign CourseDTO status deserialized as CLOSED" ($r.Json.data.course.status -eq "CLOSED")
    }
}

# 12. CANCEL ENROLLMENTS
if ($script:EnrollmentId) {
    Test-Api "Cancel enrollment" "DELETE" "/api/enrollments/$($script:EnrollmentId)" 200 $null 1 | Out-Null

    $r = Test-Api "Verify enrollment status CANCELLED" "GET" "/api/enrollments/$($script:EnrollmentId)" 200 $null 1
    Test-Condition "Enrollment is CANCELLED after DELETE" ($r.Json.data.status -eq "CANCELLED")
}
if ($script:Enrollment2Id) {
    Test-Api "Cancel second enrollment" "DELETE" "/api/enrollments/$($script:Enrollment2Id)" 200 $null 1 | Out-Null
}

# 13. NOT FOUND
if (-not $SkipNegativeTests) {
    $missingId = 999999999
    Test-Api "Course not found returns 404" "GET" "/api/courses/$missingId" 404 $null 4 | Out-Null
    Test-Api "Close course not found returns 404" "DELETE" "/api/courses/$missingId" 404 $null 4 | Out-Null
    Test-Api "Enrollment not found returns 404" "GET" "/api/enrollments/$missingId" 404 $null 4 | Out-Null
    Test-Api "Cancel enrollment not found returns 404" "DELETE" "/api/enrollments/$missingId" 404 $null 4 | Out-Null
}

# SUMMARY
Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host " TEST SUMMARY" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Passed : $script:Passed" -ForegroundColor Green
Write-Host "Failed : $script:Failed" -ForegroundColor Red
Write-Host "Total  : $($script:Passed + $script:Failed)"
Write-Host ""
$script:Results | Format-Table -AutoSize

if ($script:Failed -gt 0) {
    Write-Host "Some tests failed. Check the response and service logs." -ForegroundColor Red
    exit 1
}

Write-Host "ALL TESTS PASSED." -ForegroundColor Green
Write-Host "Test data is kept in SQL Server. Course IDs: $script:CourseId, $script:DraftCourseId, $script:DefaultCourseId, $script:Course2Id; Enrollment IDs: $script:EnrollmentId, $script:Enrollment2Id"
exit 0

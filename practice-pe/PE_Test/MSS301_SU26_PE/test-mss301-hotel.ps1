# Run to test: powershell -ExecutionPolicy Bypass -File .\test-mss301-hotel.ps1

param(
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$SkipNegativeTests
)

$ErrorActionPreference = "Stop"
$script:Passed = 0
$script:Failed = 0
$script:Results = @()
$script:RoomId = $null
$script:MaintRoomId = $null
$script:ReservationId = $null
$script:Reservation2Id = $null

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
Write-Host " MSS301 FULL API TEST - ROOM + RESERVATION" -ForegroundColor Cyan
Write-Host " Gateway: $BaseUrl" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# 0. Gateway connectivity
$healthCheck = Invoke-Api -Method GET -Path "/api/rooms?page=0&size=1"
if ($healthCheck.HttpStatus -ne 200) {
    Write-Host "Cannot reach the API correctly at $BaseUrl (HTTP $($healthCheck.HttpStatus))." -ForegroundColor Red
    Write-Host "Start SQL Server, Room Service (8081), Reservation Service (8082), then Gateway (8080)." -ForegroundColor Yellow
    exit 1
}
Test-Condition "Gateway and Room Service reachable" $true

# Unique test values (roomNumber <= 20 chars)
$suffix      = (Get-Date).ToString("MMddHHmmss")
$roomNumber  = "T$suffix"
$maintNumber = "M$suffix"
$checkIn     = (Get-Date).Date.AddDays(2).ToString("yyyy-MM-dd")
$checkOut    = (Get-Date).Date.AddDays(5).ToString("yyyy-MM-dd")   # 3 nights
$checkOut2   = (Get-Date).Date.AddDays(7).ToString("yyyy-MM-dd")
$guestName   = "Guest $suffix"
$guestNameEnc = [uri]::EscapeDataString($guestName)

$availableRoomBody = @{
    roomNumber    = $roomNumber
    roomType      = "DOUBLE"
    pricePerNight = 100
    capacity      = 2
    floor         = 3
    status        = "AVAILABLE"
}

$maintRoomBody = @{
    roomNumber    = $maintNumber
    roomType      = "SUITE"
    pricePerNight = 250.5
    capacity      = 4
    floor         = 5
    status        = "MAINTENANCE"
}

# ==================== ROOM ====================

# 1. ROOM - CREATE
$r = Test-Api "Create AVAILABLE room" "POST" "/api/rooms" 201 $availableRoomBody 1
if ($r.Json -and $r.Json.data) { $script:RoomId = [long]$r.Json.data.roomId }
Test-Condition "Created room returns roomId" ($null -ne $script:RoomId)

$r = Test-Api "Create MAINTENANCE room" "POST" "/api/rooms" 201 $maintRoomBody 1
if ($r.Json -and $r.Json.data) { $script:MaintRoomId = [long]$r.Json.data.roomId }
Test-Condition "Created MAINTENANCE room returns roomId" ($null -ne $script:MaintRoomId)

# 2. ROOM - GET DETAIL
if ($script:RoomId) {
    $r = Test-Api "Get room detail" "GET" "/api/rooms/$($script:RoomId)" 200 $null 1
    Test-Condition "Room detail has DOUBLE Enum type" ($r.Json.data.roomType -eq "DOUBLE")
    Test-Condition "Room detail has AVAILABLE Enum status" ($r.Json.data.status -eq "AVAILABLE")
} else {
    Test-Condition "Get room detail" $false "Skipped: room was not created"
}

# 3. ROOM - LIST / FILTERS
$r = Test-Api "List rooms with pagination" "GET" "/api/rooms?page=0&size=10" 200 $null 1
Test-Condition "Room list contains content array" ($null -ne $r.Json.data.content)

$r = Test-Api "Filter rooms by roomNumber" "GET" "/api/rooms?page=0&size=10&roomNumber=$roomNumber" 200 $null 1
Test-Condition "roomNumber filter returns matching room" (@($r.Json.data.content | Where-Object { $_.roomNumber -eq $roomNumber }).Count -eq 1)

$r = Test-Api "Filter rooms by Enum roomType DOUBLE" "GET" "/api/rooms?page=0&size=100&roomType=DOUBLE" 200 $null 1
Test-Condition "roomType filter returns only DOUBLE rooms" (@($r.Json.data.content | Where-Object { $_.roomType -ne "DOUBLE" }).Count -eq 0)

$r = Test-Api "Filter rooms by Enum status AVAILABLE" "GET" "/api/rooms?page=0&size=100&status=AVAILABLE" 200 $null 1
Test-Condition "status filter returns only AVAILABLE rooms" (@($r.Json.data.content | Where-Object { $_.status -ne "AVAILABLE" }).Count -eq 0)

$r = Test-Api "Filter rooms by roomNumber + roomType + status" "GET" "/api/rooms?page=0&size=10&roomNumber=$roomNumber&roomType=DOUBLE&status=AVAILABLE" 200 $null 1
Test-Condition "Combined room filter finds test room" (@($r.Json.data.content | Where-Object { $_.roomId -eq $script:RoomId }).Count -eq 1)

# 4. ROOM - UPDATE PARTIAL
if ($script:RoomId) {
    $r = Test-Api "Update room price and capacity" "PUT" "/api/rooms/$($script:RoomId)" 200 @{
        pricePerNight = 120
        capacity      = 3
    } 1
    Test-Condition "Room update persisted price" ([decimal]$r.Json.data.pricePerNight -eq 120)
    Test-Condition "Room update persisted capacity" ($r.Json.data.capacity -eq 3)
    Test-Condition "Room update kept roomNumber unchanged" ($r.Json.data.roomNumber -eq $roomNumber)
}

# 5. ROOM - NEGATIVE TESTS
if (-not $SkipNegativeTests) {
    $dup = $availableRoomBody.Clone()
    Test-Api "Reject duplicate roomNumber (HTTP 226)" "POST" "/api/rooms" 226 $dup 3 | Out-Null

    if ($script:MaintRoomId) {
        Test-Api "Reject update to duplicate roomNumber (HTTP 226)" "PUT" "/api/rooms/$($script:MaintRoomId)" 226 @{ roomNumber = $roomNumber } 3 | Out-Null
        Test-Api "Reject update room capacity > 10" "PUT" "/api/rooms/$($script:MaintRoomId)" 406 @{ capacity = 11 } 2 | Out-Null
    }

    $bad = $availableRoomBody.Clone(); $bad.roomNumber = "P$suffix"; $bad.pricePerNight = 0
    Test-Api "Reject room price = 0" "POST" "/api/rooms" 406 $bad 2 | Out-Null

    $bad = $availableRoomBody.Clone(); $bad.roomNumber = "C$suffix"; $bad.capacity = 11
    Test-Api "Reject room capacity = 11" "POST" "/api/rooms" 406 $bad 2 | Out-Null

    $bad = $availableRoomBody.Clone(); $bad.roomNumber = "C0$suffix"; $bad.capacity = 0
    Test-Api "Reject room capacity = 0" "POST" "/api/rooms" 406 $bad 2 | Out-Null

    $bad = $availableRoomBody.Clone(); $bad.roomNumber = "F$suffix"; $bad.floor = 0
    Test-Api "Reject room floor = 0" "POST" "/api/rooms" 406 $bad 2 | Out-Null

    $bad = $availableRoomBody.Clone(); $bad.roomNumber = "E$suffix"; $bad.roomType = "PENTHOUSE"
    Test-Api "Reject invalid RoomType Enum" "POST" "/api/rooms" 406 $bad 2 | Out-Null

    $bad = $availableRoomBody.Clone(); $bad.roomNumber = "S$suffix"; $bad.status = "NOT_A_STATUS"
    Test-Api "Reject invalid RoomStatus Enum" "POST" "/api/rooms" 406 $bad 2 | Out-Null

    $bad = $availableRoomBody.Clone(); $bad.Remove("roomNumber")
    Test-Api "Reject room without roomNumber" "POST" "/api/rooms" 406 $bad 2 | Out-Null

    Test-Api "Reject invalid room pagination" "GET" "/api/rooms?page=-1&size=10" 406 $null 2 | Out-Null
    Test-Api "Reject invalid room status query" "GET" "/api/rooms?status=UNKNOWN" 406 $null 2 | Out-Null
    Test-Api "Reject invalid room type query" "GET" "/api/rooms?roomType=UNKNOWN" 406 $null 2 | Out-Null
}

# ==================== RESERVATION ====================

# 6. RESERVATION - CREATE ON AVAILABLE ROOM (total is computed: 3 nights x 120)
if ($script:RoomId) {
    $reservationBody = @{
        guestName      = $guestName
        guestEmail     = "guest$suffix@example.com"
        guestPhone     = "0901234567"
        checkInDate    = $checkIn
        checkOutDate   = $checkOut
        numberOfGuests = 2
        roomId         = $script:RoomId
    }

    $r = Test-Api "Create reservation in AVAILABLE room" "POST" "/api/reservations" 201 $reservationBody 1
    if ($r.Json -and $r.Json.data) { $script:ReservationId = [long]$r.Json.data.reservationId }
    Test-Condition "Created reservation returns reservationId" ($null -ne $script:ReservationId)
    Test-Condition "Reservation default status is CONFIRMED" ($r.Json.data.status -eq "CONFIRMED")
    Test-Condition "Reservation totalAmount computed = nights x price (360)" ([decimal]$r.Json.data.totalAmount -eq 360)

    $explicit = $reservationBody.Clone()
    $explicit.guestName    = "$guestName Two"
    $explicit.checkOutDate = $checkOut2
    $explicit.totalAmount  = 999.5
    $r = Test-Api "Create reservation with explicit totalAmount" "POST" "/api/reservations" 201 $explicit 1
    if ($r.Json -and $r.Json.data) { $script:Reservation2Id = [long]$r.Json.data.reservationId }
    Test-Condition "Explicit totalAmount is kept (999.5)" ([decimal]$r.Json.data.totalAmount -eq 999.5)
} else {
    Test-Condition "Create reservation in AVAILABLE room" $false "Skipped: room was not created"
}

# 7. RESERVATION - DETAIL / FEIGN
if ($script:ReservationId) {
    $r = Test-Api "Get reservation detail with Feign RoomDTO" "GET" "/api/reservations/$($script:ReservationId)" 200 $null 1
    Test-Condition "Reservation detail embeds room data" ($null -ne $r.Json.data.room)
    Test-Condition "Feign RoomDTO roomNumber is correct" ($r.Json.data.room.roomNumber -eq $roomNumber)
    Test-Condition "Feign RoomDTO status deserialized as AVAILABLE" ($r.Json.data.room.status -eq "AVAILABLE")
    Test-Condition "Reservation checkInDate format yyyy-MM-dd" ($r.Json.data.checkInDate -eq $checkIn)
}

# 8. RESERVATION - UPDATE
if ($script:ReservationId) {
    $r = Test-Api "Update reservation guestName" "PUT" "/api/reservations/$($script:ReservationId)" 200 @{
        guestName = "Guest Updated $suffix"
    } 1
    Test-Condition "Reservation guestName updated" ($r.Json.data.guestName -eq "Guest Updated $suffix")

    $r = Test-Api "Update reservation status CHECKED_IN" "PUT" "/api/reservations/$($script:ReservationId)" 200 @{
        status = "CHECKED_IN"
    } 1
    Test-Condition "Reservation status updated to CHECKED_IN" ($r.Json.data.status -eq "CHECKED_IN")

    $r = Test-Api "Update reservation dates and guests" "PUT" "/api/reservations/$($script:ReservationId)" 200 @{
        checkOutDate   = $checkOut2
        numberOfGuests = 1
    } 1
    Test-Condition "Reservation checkOutDate updated" ($r.Json.data.checkOutDate -eq $checkOut2)
    Test-Condition "Reservation numberOfGuests updated" ($r.Json.data.numberOfGuests -eq 1)

    # Restore name + CONFIRMED for list filter tests
    Test-Api "Restore reservation status CONFIRMED" "PUT" "/api/reservations/$($script:ReservationId)" 200 @{
        guestName = $guestName
        status    = "CONFIRMED"
    } 1 | Out-Null
}

# 9. RESERVATION - LIST / FILTERS
$r = Test-Api "List reservations with pagination" "GET" "/api/reservations?page=0&size=10" 200 $null 1
Test-Condition "Reservation list contains content array" ($null -ne $r.Json.data.content)

$r = Test-Api "Filter reservations by guestName" "GET" "/api/reservations?page=0&size=10&guestName=$guestNameEnc" 200 $null 1
Test-Condition "guestName filter returns test records (2)" (@($r.Json.data.content).Count -eq 2)
Test-Condition "Filtered reservations embed room data" (@($r.Json.data.content | Where-Object { $null -eq $_.room }).Count -eq 0)

$r = Test-Api "Filter reservations by status CONFIRMED" "GET" "/api/reservations?page=0&size=100&status=CONFIRMED" 200 $null 1
Test-Condition "status filter returns only CONFIRMED" (@($r.Json.data.content | Where-Object { $_.status -ne "CONFIRMED" }).Count -eq 0)

if ($script:ReservationId) {
    $r = Test-Api "Filter reservations by guestName and status" "GET" "/api/reservations?page=0&size=10&guestName=$guestNameEnc&status=CONFIRMED" 200 $null 1
    Test-Condition "Combined reservation filter finds test record" (@($r.Json.data.content | Where-Object { $_.reservationId -eq $script:ReservationId }).Count -eq 1)
}

# 10. RESERVATION - NEGATIVE TESTS
if (-not $SkipNegativeTests) {
    if ($script:MaintRoomId) {
        $body = @{
            guestName = "Rejected $suffix"; guestEmail = "rej$suffix@example.com"; guestPhone = "0900000000"
            checkInDate = $checkIn; checkOutDate = $checkOut; numberOfGuests = 1
            roomId = $script:MaintRoomId
        }
        Test-Api "Reject reservation in MAINTENANCE room" "POST" "/api/reservations" 400 $body 5 | Out-Null

        if ($script:ReservationId) {
            Test-Api "Reject moving reservation to MAINTENANCE room" "PUT" "/api/reservations/$($script:ReservationId)" 400 @{ roomId = $script:MaintRoomId } 5 | Out-Null
        }
    }

    if ($script:RoomId) {
        $base = @{
            guestName = "Neg $suffix"; guestEmail = "neg$suffix@example.com"; guestPhone = "0900000001"
            checkInDate = $checkIn; checkOutDate = $checkOut; numberOfGuests = 1
            roomId = $script:RoomId
        }

        $b = $base.Clone(); $b.guestEmail = "not-an-email"
        Test-Api "Reject invalid guestEmail" "POST" "/api/reservations" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.guestPhone = "1" * 21
        Test-Api "Reject guestPhone longer than 20" "POST" "/api/reservations" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.Remove("guestName")
        Test-Api "Reject reservation without guestName" "POST" "/api/reservations" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.checkOutDate = $checkIn
        Test-Api "Reject checkOutDate = checkInDate" "POST" "/api/reservations" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.checkOutDate = (Get-Date).Date.AddDays(1).ToString("yyyy-MM-dd")
        Test-Api "Reject checkOutDate before checkInDate" "POST" "/api/reservations" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.checkInDate = "not-a-date"
        Test-Api "Reject invalid date format" "POST" "/api/reservations" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.numberOfGuests = 0
        Test-Api "Reject numberOfGuests = 0" "POST" "/api/reservations" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.totalAmount = -1
        Test-Api "Reject negative totalAmount" "POST" "/api/reservations" 406 $b 2 | Out-Null

        $b = $base.Clone(); $b.status = "INVALID"
        Test-Api "Reject invalid ReservationStatus Enum" "POST" "/api/reservations" 406 $b 2 | Out-Null

        if ($script:ReservationId) {
            Test-Api "Reject update with checkOut before checkIn" "PUT" "/api/reservations/$($script:ReservationId)" 406 @{ checkOutDate = (Get-Date).Date.ToString("yyyy-MM-dd") } 2 | Out-Null
            Test-Api "Reject update with invalid status Enum" "PUT" "/api/reservations/$($script:ReservationId)" 406 @{ status = "INVALID" } 2 | Out-Null
        }
    }

    $b = @{
        guestName = "NoRoom $suffix"; guestEmail = "noroom$suffix@example.com"; guestPhone = "0900000002"
        checkInDate = $checkIn; checkOutDate = $checkOut; numberOfGuests = 1
        roomId = 999999999
    }
    Test-Api "Reject reservation for non-existing room (404)" "POST" "/api/reservations" 404 $b 4 | Out-Null

    Test-Api "Reject invalid reservation pagination" "GET" "/api/reservations?page=-1&size=10" 406 $null 2 | Out-Null
    Test-Api "Reject invalid reservation status query" "GET" "/api/reservations?status=UNKNOWN" 406 $null 2 | Out-Null
}

# 11. DELETE ROOM (soft delete -> MAINTENANCE), THEN VERIFY ROOM REJECTION
if ($script:RoomId) {
    Test-Api "Delete room" "DELETE" "/api/rooms/$($script:RoomId)" 200 $null 1 | Out-Null

    $r = Test-Api "Verify room status MAINTENANCE" "GET" "/api/rooms/$($script:RoomId)" 200 $null 1
    Test-Condition "Room is MAINTENANCE after DELETE" ($r.Json.data.status -eq "MAINTENANCE")

    if (-not $SkipNegativeTests) {
        $b = @{
            guestName = "AfterDelete $suffix"; guestEmail = "after$suffix@example.com"; guestPhone = "0900000003"
            checkInDate = $checkIn; checkOutDate = $checkOut; numberOfGuests = 1
            roomId = $script:RoomId
        }
        Test-Api "Reject reservation in deleted (MAINTENANCE) room" "POST" "/api/reservations" 400 $b 5 | Out-Null
    }
}

# 12. CANCEL RESERVATIONS
if ($script:ReservationId) {
    Test-Api "Cancel reservation" "DELETE" "/api/reservations/$($script:ReservationId)" 200 $null 1 | Out-Null

    $r = Test-Api "Verify reservation status CANCELLED" "GET" "/api/reservations/$($script:ReservationId)" 200 $null 1
    Test-Condition "Reservation is CANCELLED after DELETE" ($r.Json.data.status -eq "CANCELLED")
}
if ($script:Reservation2Id) {
    Test-Api "Cancel second reservation" "DELETE" "/api/reservations/$($script:Reservation2Id)" 200 $null 1 | Out-Null
}

# 13. NOT FOUND
if (-not $SkipNegativeTests) {
    $missingId = 999999999
    Test-Api "Room not found returns 404" "GET" "/api/rooms/$missingId" 404 $null 4 | Out-Null
    Test-Api "Update room not found returns 404" "PUT" "/api/rooms/$missingId" 404 @{ capacity = 2 } 4 | Out-Null
    Test-Api "Delete room not found returns 404" "DELETE" "/api/rooms/$missingId" 404 $null 4 | Out-Null
    Test-Api "Reservation not found returns 404" "GET" "/api/reservations/$missingId" 404 $null 4 | Out-Null
    Test-Api "Update reservation not found returns 404" "PUT" "/api/reservations/$missingId" 404 @{ guestName = "X" } 4 | Out-Null
    Test-Api "Cancel reservation not found returns 404" "DELETE" "/api/reservations/$missingId" 404 $null 4 | Out-Null
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
Write-Host "Test data is kept in SQL Server. Room IDs: $script:RoomId, $script:MaintRoomId; Reservation IDs: $script:ReservationId, $script:Reservation2Id"
exit 0

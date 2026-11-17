IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'MSS301_2026_PE_RE')
BEGIN
    CREATE DATABASE [MSS301_2026_PE_RE];
END
GO

USE [MSS301_2026_PE_RE]
GO

--------------------------------------------------------------------------------
-- 1. CREATE TABLE: [dbo].[courses]
--------------------------------------------------------------------------------
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[courses](
	[course_id] [bigint] IDENTITY(1,1) NOT NULL,
	[course_code] [nvarchar](20) NOT NULL,
	[title] [nvarchar](150) NOT NULL,
	[description] [nvarchar](max) NULL,
	[credits] [int] NOT NULL,
	[status] [nvarchar](10) NOT NULL,
	[start_date] [date] NOT NULL,
	[end_date] [date] NOT NULL,
	[instructor_id] [bigint] NULL,
	CONSTRAINT [PK_courses] PRIMARY KEY CLUSTERED ([course_id] ASC)
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

--------------------------------------------------------------------------------
-- 2. CREATE TABLE: [dbo].[enrollments]
--------------------------------------------------------------------------------
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[enrollments](
	[enrollment_id] [bigint] IDENTITY(1,1) NOT NULL,
	[student_id] [bigint] NOT NULL,
	[student_name] [nvarchar](100) NOT NULL,
	[student_email] [nvarchar](100) NOT NULL,
	[enrollment_date] [date] NOT NULL,
	[status] [nvarchar](15) NOT NULL,
	[grade] [nvarchar](5) NULL,
	[course_id] [bigint] NOT NULL,
	CONSTRAINT [PK_enrollments] PRIMARY KEY CLUSTERED ([enrollment_id] ASC)
) ON [PRIMARY]
GO

--------------------------------------------------------------------------------
-- 3. CONSTRAINTS & INDEXES
--------------------------------------------------------------------------------
-- Unique code for courses
ALTER TABLE [dbo].[courses] ADD CONSTRAINT [UQ_courses_course_code] UNIQUE NONCLUSTERED ([course_code] ASC);
GO

-- Check constraints for courses
ALTER TABLE [dbo].[courses] WITH CHECK ADD CONSTRAINT [CK_courses_credits] CHECK (([credits] >= (1) AND [credits] <= (10)));
GO
ALTER TABLE [dbo].[courses] CHECK CONSTRAINT [CK_courses_credits];
GO

ALTER TABLE [dbo].[courses] WITH CHECK ADD CONSTRAINT [CK_courses_date_range] CHECK (([end_date] > [start_date]));
GO
ALTER TABLE [dbo].[courses] CHECK CONSTRAINT [CK_courses_date_range];
GO

ALTER TABLE [dbo].[courses] WITH CHECK ADD CONSTRAINT [CK_courses_status] CHECK (([status] = N'DRAFT' OR [status] = N'CLOSED' OR [status] = N'OPEN'));
GO
ALTER TABLE [dbo].[courses] CHECK CONSTRAINT [CK_courses_status];
GO

-- Check constraints for enrollments
ALTER TABLE [dbo].[enrollments] WITH CHECK ADD CONSTRAINT [CK_enrollments_grade] CHECK (([grade] IS NULL OR ([grade] = N'F' OR [grade] = N'D' OR [grade] = N'C' OR [grade] = N'B' OR [grade] = N'A')));
GO
ALTER TABLE [dbo].[enrollments] CHECK CONSTRAINT [CK_enrollments_grade];
GO

ALTER TABLE [dbo].[enrollments] WITH CHECK ADD CONSTRAINT [CK_enrollments_status] CHECK (([status] = N'CANCELLED' OR [status] = N'COMPLETED' OR [status] = N'ACTIVE'));
GO
ALTER TABLE [dbo].[enrollments] CHECK CONSTRAINT [CK_enrollments_status];
GO

-- Foreign key relationship
ALTER TABLE [dbo].[enrollments] WITH CHECK ADD CONSTRAINT [FK_enrollments_courses] FOREIGN KEY([course_id])
REFERENCES [dbo].[courses] ([course_id]);
GO
ALTER TABLE [dbo].[enrollments] CHECK CONSTRAINT [FK_enrollments_courses];
GO

-- Indexes
CREATE NONCLUSTERED INDEX [IX_courses_status] ON [dbo].[courses] ([status] ASC);
GO
CREATE NONCLUSTERED INDEX [IX_courses_title] ON [dbo].[courses] ([title] ASC);
GO
CREATE NONCLUSTERED INDEX [IX_enrollments_course_id] ON [dbo].[enrollments] ([course_id] ASC);
GO
CREATE NONCLUSTERED INDEX [IX_enrollments_status] ON [dbo].[enrollments] ([status] ASC);
GO
CREATE NONCLUSTERED INDEX [IX_enrollments_student_name] ON [dbo].[enrollments] ([student_name] ASC);
GO

--------------------------------------------------------------------------------
-- 4. SAMPLE DATA
--------------------------------------------------------------------------------
SET IDENTITY_INSERT [dbo].[courses] ON;
INSERT INTO [dbo].[courses] ([course_id], [course_code], [title], [description], [credits], [status], [start_date], [end_date], [instructor_id]) VALUES 
(1, N'T1112181513', N'Gateway Test Course Updated 1112181513', N'Created by automated PowerShell test', 4, N'OPEN', CAST(N'2026-11-14' AS Date), CAST(N'2027-02-10' AS Date), 1001),
(2, N'D1112181513', N'Gateway Draft Course 1112181513', N'Draft course for negative enrollment test', 2, N'DRAFT', CAST(N'2026-11-14' AS Date), CAST(N'2027-02-10' AS Date), 1002),
(3, N'T1112181645', N'Gateway Test Course Updated 1112181645', N'Created by automated PowerShell test', 4, N'CLOSED', CAST(N'2026-11-14' AS Date), CAST(N'2027-02-10' AS Date), 1001),
(4, N'D1112181645', N'Gateway Draft Course 1112181645', N'Draft course for negative enrollment test', 2, N'DRAFT', CAST(N'2026-11-14' AS Date), CAST(N'2027-02-10' AS Date), 1002);
SET IDENTITY_INSERT [dbo].[courses] OFF;
GO

SET IDENTITY_INSERT [dbo].[enrollments] ON;
INSERT INTO [dbo].[enrollments] ([enrollment_id], [student_id], [student_name], [student_email], [enrollment_date], [status], [grade], [course_id]) VALUES 
(1, 190284, N'PowerShell Test Student 1112181513', N'test1112181513@example.com', CAST(N'2026-11-12' AS Date), N'ACTIVE', N'A', 1),
(2, 190284, N'PowerShell Test Student 1112181645', N'test1112181645@example.com', CAST(N'2026-11-12' AS Date), N'CANCELLED', N'B', 3);
SET IDENTITY_INSERT [dbo].[enrollments] OFF;
GO
-------------------------------Prepare Minio---------------------------------------------

 Go to "https://min.io/docs/minio/windows/index.html" for install guide
 Or Quickstart:
 - Download: https://dl.min.io/server/minio/release/windows-amd64/minio.exe
 - Place minio.exe in any location (example: C:/minio)
 - Run PowerShell or the Command Prompt, navigate to the location (example: C:/minio)
 - Run command "minio.exe server C:/minio --console-address ":9001" or "minio.exe server C:/minio"
 - Now minio server running, to stop minio server use Ctrl+C (2 times if first time not work)
 - Set bucket "Public":
    + Go to "local:9000"
    + Log into MinIO with account show in Command Prompt (example: minioadmin)
    + Go to Buckets tag in left navbar.
    + If buckets is empty, "Create Bucket" and name it "sloc-system".
    + Select bucket "sloc-system".
    + Change "Access Policy" to "Public"

-------------------------------Prepare Database------------------------------------------

- Install MySQL
- In application.properties, change MySQL Database details to your MySQL Database details
- Open MySQL Command Line Client
- Run mysql codes in database.txt to create database and tables

-------------------------------Run Application------------------------------------------
- Run application (SlocSystemApplication.java)
- Open in browser - http://localhost:8080/

--------------------------------Username : Password--------------------------------------
admin/adminpassword
moderator_01/modpassword
instructor_01/instructorpassword
student_01/studentpassword
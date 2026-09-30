# Restricted Google Sheets

Restricted sheets use a Google Cloud service account. A standard API key cannot grant access to a private spreadsheet.

The integration is read-only: it requests only the `spreadsheets.readonly` OAuth scope and uses Google Sheets GET requests. It has no code to edit, append, or delete spreadsheet data. Share the sheet as **Viewer**, not Editor. Sync updates the local inventory database only; removing a saved sheet link removes only that connection from this app.

1. In Google Cloud Console, enable the Google Sheets API and create a service account.
2. Create a JSON key for the service account and store it outside source control. The `credentials/` directory and service-account JSON files are ignored by Git in this backend.
3. Share the spreadsheet with the service account's email address as a Viewer.
4. Set the credential file path in the PowerShell session used to start the backend:

   ```powershell
   $env:GOOGLE_APPLICATION_CREDENTIALS = 'C:\secure\sheets-reader-service-account.json'
   .\mvnw.cmd spring-boot:run
   ```

`application.properties` reads this environment variable into `google.sheets.credentials-path`. The JSON key contents must not be placed in `application.properties`, committed to Git, or shared in chat.

When the variable is empty, the importer continues using the existing public-sheet CSV access. Restart the backend after changing the credential path.

The service account email is `sheets-reader@inventory-sync-510011.iam.gserviceaccount.com`.

## Run the backend as a Windows service with WinSW 4

The PowerShell environment variable above applies only to processes started from that PowerShell session. A Windows service needs its own environment configuration and permission to read the JSON key.

1. Build the executable JAR and create a deployment directory outside the repository. Download the WinSW 4 executable, rename it to `NextFirstInventoryService.exe`, and place it with a matching `NextFirstInventoryService.xml` in the deployment directory.

   ```powershell
   .\mvnw.cmd clean package
   New-Item -ItemType Directory -Force C:\Services\NextFirstInventory\logs
   Copy-Item .\target\NextFirstInventory.jar C:\Services\NextFirstInventory\
   ```

2. Create `C:\Services\NextFirstInventory\NextFirstInventoryService.xml` beside the WinSW executable. Change the Java path if Java 17 is installed elsewhere.

   ```xml
   <service>
     <id>NextFirstInventory</id>
     <name>NextFirstInventory Backend</name>
     <description>NextFirst inventory backend</description>
     <executable>C:\Program Files\Java\jdk-17\bin\java.exe</executable>
   <arguments>-jar "C:\Services\NextFirstInventory\NextFirstInventory.jar"</arguments>
     <workingdirectory>C:\Services\NextFirstInventory</workingdirectory>
     <env name="GOOGLE_APPLICATION_CREDENTIALS" value="C:\secure\sheets-reader-service-account.json" />
     <logpath>C:\Services\NextFirstInventory\logs</logpath>
     <log mode="roll" />
     <onfailure action="restart" delay="10 sec" />
     <startmode>Automatic</startmode>
   </service>
   ```

3. Configure the service to log on as a dedicated Windows account using Services (`services.msc`), rather than granting broad access to the credential. Give that account read access to the JSON key and write access to the service log directory. Keep the key and service deployment files outside source control.
4. From an elevated PowerShell window, install and start the service:

   ```powershell
   Set-Location C:\Services\NextFirstInventory
   .\NextFirstInventoryService.exe install
   .\NextFirstInventoryService.exe start
   .\NextFirstInventoryService.exe status
   ```

Make sure PostgreSQL is running and reachable by the service. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for the service if its database settings differ from the defaults in `application.properties`. Check the WinSW logs under `C:\Services\NextFirstInventory\logs` if the service does not start.


# how to create the google cloude service steps
1. Create a Google Cloud project
Go to Google Cloud Console.

Sign in with your Google account.

At the top, click the project selector.

Click New Project.

Absolutely. The setup is basically Google Cloud → service account → JSON key → share Sheet → configure Windows → start your Spring Boot backend.

# 1. Create a Google Cloud project
Go to Google Cloud Console.

Sign in with your Google account.

At the top, click the project selector.

Click New Project.

Give it a name, for example:
Restricted Sheets Reader

Click Create.

Make sure the new project is selected.

# 2. Enable Google Sheets API
In Google Cloud Console:

Open APIs & Services → Library.

Search for Google Sheets API.

Open it.

Click Enable.

You don't need an API key for the service-account approach described in your instructions.

# 3. Create the service account
Go to IAM & Admin → Service Accounts.

Click Create Service Account.

Name it something like:
sheets-reader

Click Create and Continue.

For this setup, you generally don't need to grant it a project role.

Click Done.

You'll see an email address similar to:

sheets-reader@your-project-id.iam.gserviceaccount.com

Copy that email address. You'll need it when sharing the spreadsheet.

# 4. Create the JSON credentials
Open the service account you just created.

Go to the Keys tab.

Click Add Key → Create new key.

Select JSON.

Click Create.

Google will download a .json file.

Important
Treat this JSON file like a password.

Don't:

upload it to GitHub

put it in application.properties

paste its contents into ChatGPT

send it to anyone

commit it to your repository

Your instructions indicate that the backend already ignores the credentials/ directory and service-account JSON files, which is good.

For example, you could store it at:

C:\secure\sheets-reader-service-account.json

# 5. Share your Google Sheet with the service account
Open the private Google Sheet you want your application to read.

Click Share.

Add the service-account email from step 3, for example:

sheets-reader@your-project-id.iam.gserviceaccount.com

Set the permission to:

Viewer

Then click Send/Share.

You do not need to make the spreadsheet public.


   

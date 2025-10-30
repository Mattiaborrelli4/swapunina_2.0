@echo off
echo ===============================================
echo Scaricamento ed estrazione librerie per SwapUnina
echo ===============================================
echo.

REM Creiamo la cartella librerie se non esiste
if not exist "librerie" mkdir librerie

REM ================================
REM Controllo se 7-Zip è installato
REM ================================
where 7z >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRORE: 7-Zip non trovato nel PATH. Installa 7-Zip prima di eseguire questo script.
    pause
    exit /b
)

REM ================================
REM JavaFX SDK 24.0.1
REM ================================
echo Scaricamento JavaFX SDK...
if not exist "librerie\javafx-sdk-24.0.1" (
    powershell -Command "Invoke-WebRequest -Uri https://gluonhq.com/download/javafx-24-0-1-sdk-windows/ -OutFile librerie\javafx-sdk.zip"
    7z x librerie\javafx-sdk.zip -olibrerie\javafx-sdk-24.0.1
    del librerie\javafx-sdk.zip
) else (
    echo JavaFX SDK già presente.
)

REM ================================
REM FFmpeg 7.1.1 Full Build
REM ================================
echo Scaricamento FFmpeg...
if not exist "librerie\ffmpeg-7.1.1-full_build" (
    powershell -Command "Invoke-WebRequest -Uri https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-full.7z -OutFile librerie\ffmpeg.7z"
    7z x librerie\ffmpeg.7z -olibrerie\ffmpeg-7.1.1-full_build
    del librerie\ffmpeg.7z
) else (
    echo FFmpeg già presente.
)

REM ================================
REM jBCrypt 0.4
REM ================================
echo Scaricamento jBCrypt...
if not exist "librerie\jBCrypt-0.4.jar" (
    powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar -OutFile librerie\jBCrypt-0.4.jar"
)

REM ================================
REM Cloudinary
REM ================================
echo Scaricamento Cloudinary...
if not exist "librerie\cloudinary-core-1.38.0.jar" (
    powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/com/cloudinary/cloudinary-core/1.38.0/cloudinary-core-1.38.0.jar -OutFile librerie\cloudinary-core-1.38.0.jar"
)
if not exist "librerie\cloudinary-http44-1.33.0.jar" (
    powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/com/cloudinary/cloudinary-http44/1.33.0/cloudinary-http44-1.33.0.jar -OutFile librerie\cloudinary-http44-1.33.0.jar"
)

REM ================================
REM Apache Commons e JSON
REM ================================
echo Scaricamento Apache Commons e JSON...
powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/commons-codec/commons-codec/1.15/commons-codec-1.15.jar -OutFile librerie\commons-codec-1.15.jar"
powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar -OutFile librerie\commons-io-2.11.0.jar"
powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar -OutFile librerie\commons-logging-1.2.jar"
powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar -OutFile librerie\httpclient-4.5.13.jar"
powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.13/httpcore-4.4.13.jar -OutFile librerie\httpcore-4.4.13.jar"
powershell -Command "Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar -OutFile librerie\json-20230227.jar"

REM ================================
REM Messaggio finale
REM ================================
echo.
echo =============================================
echo Tutte le librerie sono state scaricate ed estratte automaticamente.
echo Controlla che Java JDK e Maven siano correttamente configurati nel PATH.
echo =============================================
pause

@echo on

set folder=backups
set d=%date:~6,4%-%date:~3,2%-%date:~0,2%
set t=%time: =0%
set tn=%t:~0,2%-%t:~3,2%-%t:~6,2%
set filename=%folder%/portal_%d%_%tn%.7z

7z a -r -t7z -mx=9 %filename% @backup.list

if errorlevel 1 goto exit

del *.log
rd /S /Q log

:exit
pause

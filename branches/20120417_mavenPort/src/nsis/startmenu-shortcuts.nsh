#  If you provide this file, the installer will allow the user to choose
#  whether or not to create shortcuts in the <code>Start Menu</code>.
#  The shortcuts specified in this file will be created.
SetOutPath $INSTDIR\scarab\WEB-INF\conf
CreateShortCut "${PROJECT_STARTMENU_FOLDER}\Create Database.lnk" "$INSTDIR\scarab\WEB-INF\conf\create-db.bat" "" "$INSTDIR\scarab\WEB-INF\conf\create-db.bat" 0

SetOutPath $INSTDIR\tomcat\bin
CreateShortCut "${PROJECT_STARTMENU_FOLDER}\Start Server.lnk" "$INSTDIR\tomcat\bin\startup.bat" "" "$INSTDIR\tomcat\bin\startup.bat" 0
CreateShortCut "${PROJECT_STARTMENU_FOLDER}\Stop Server.lnk" "$INSTDIR\tomcat\bin\shutdown.bat" "" "$INSTDIR\tomcat\bin\shutdown.bat" 0
CreateShortCut "${PROJECT_STARTMENU_FOLDER}\Scarab.lnk" "http://localhost:8080/scarab"

SetOutPath $INSTDIR
CreateShortCut "${PROJECT_STARTMENU_FOLDER}\Uninstall.lnk" "$INSTDIR\Uninst.exe" "" "$INSTDIR\Uninst.exe" 0


; Computer-Aided Solutions: Windows installer
; @author Luis Gabriel Santiago
; Build: makensis -DSRC=<portable folder> -DOUT=<setup.exe> installer.nsi

Unicode true
ManifestDPIAware true
SetCompressor /SOLID lzma
SetCompressorDictSize 64

!define APPNAME "Computer-Aided Solutions"
!define APPVERSION "1.0.0"
!define PUBLISHER "Luis Gabriel Santiago"
!define APPKEY "Software\ComputerAidedSolutions"
!define UNINSTKEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\ComputerAidedSolutions"
!define JAVAW "$INSTDIR\runtime\bin\javaw.exe"
!define JARARGS '-jar "$INSTDIR\app\computer-aided-solutions.jar"'

Name "${APPNAME}"
Caption "${APPNAME} Setup"
OutFile "${OUT}"
; Per-user install: no administrator password needed (works on school PCs too)
InstallDir "$LOCALAPPDATA\Programs\${APPNAME}"
InstallDirRegKey HKCU "${APPKEY}" "InstallDir"
RequestExecutionLevel user
BrandingText "${APPNAME} ${APPVERSION}"

VIProductVersion "1.0.0.0"
VIAddVersionKey "ProductName" "${APPNAME}"
VIAddVersionKey "CompanyName" "${PUBLISHER}"
VIAddVersionKey "FileDescription" "${APPNAME} Setup"
VIAddVersionKey "FileVersion" "${APPVERSION}"
VIAddVersionKey "ProductVersion" "${APPVERSION}"
VIAddVersionKey "LegalCopyright" "${PUBLISHER}"

!include "MUI2.nsh"
!include "LogicLib.nsh"
!include "FileFunc.nsh"

!define MUI_ICON "cas.ico"
!define MUI_UNICON "cas.ico"
!define MUI_ABORTWARNING
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_RIGHT
!define MUI_HEADERIMAGE_BITMAP "header.bmp"
!define MUI_HEADERIMAGE_UNBITMAP "header.bmp"
!define MUI_WELCOMEFINISHPAGE_BITMAP "welcome.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "welcome.bmp"

; ---------------------------------------------------------------- pages
!define MUI_WELCOMEPAGE_TITLE "Welcome to ${APPNAME}"
!define MUI_WELCOMEPAGE_TEXT "This will install ${APPNAME} on this computer.$\r$\n$\r$\nYou do not need Java or NetBeans. Everything the app needs comes with it. It only needs an internet connection to reach the database.$\r$\n$\r$\nClick Next to continue."
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_TITLE "${APPNAME} is ready"
!define MUI_FINISHPAGE_TEXT "Open it any time from the ${APPNAME} icon on your Desktop or in the Start menu.$\r$\n$\r$\nTo remove it later, use Settings > Apps > Installed apps."
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_TEXT "Open ${APPNAME} now"
!define MUI_FINISHPAGE_RUN_FUNCTION LaunchApp
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

; ---------------------------------------------------------------- helpers
; The app's window is titled exactly "Computer-Aided Solutions".
!macro WaitUntilAppClosed
  ${Do}
    FindWindow $0 "" "${APPNAME}"
    ${If} $0 = 0
      ${Break}
    ${EndIf}
    MessageBox MB_RETRYCANCEL|MB_ICONEXCLAMATION "${APPNAME} is open. Close it, then click Retry." IDRETRY +2
    Abort
  ${Loop}
!macroend

Function .onInit
  SetShellVarContext current
  !insertmacro WaitUntilAppClosed
FunctionEnd

Function un.onInit
  SetShellVarContext current
  !insertmacro WaitUntilAppClosed
FunctionEnd

Function LaunchApp
  ; Exec runs in $OUTDIR, and the app reads db.properties from there
  SetOutPath "$INSTDIR"
  Exec '"${JAVAW}" ${JARARGS}'
FunctionEnd

; ---------------------------------------------------------------- install
Section "Install"
  ; Replace the program files from an older install, if there is one
  RMDir /r "$INSTDIR\runtime"
  RMDir /r "$INSTDIR\app"

  SetOutPath "$INSTDIR"
  File "${SRC}\db.properties"
  File "${SRC}\cas.ico"
  File /r "${SRC}\runtime"
  File /r "${SRC}\app"
  WriteUninstaller "$INSTDIR\Uninstall ${APPNAME}.exe"

  ; Shortcuts start in $OUTDIR ($INSTDIR), where db.properties is
  CreateShortcut "$DESKTOP\${APPNAME}.lnk" "${JAVAW}" '${JARARGS}' "$INSTDIR\cas.ico" 0 SW_SHOWNORMAL "" "Open ${APPNAME}"
  CreateShortcut "$SMPROGRAMS\${APPNAME}.lnk" "${JAVAW}" '${JARARGS}' "$INSTDIR\cas.ico" 0 SW_SHOWNORMAL "" "Open ${APPNAME}"

  ; Show it in Settings > Apps so it can be removed like any program
  WriteRegStr HKCU "${APPKEY}" "InstallDir" "$INSTDIR"
  WriteRegStr HKCU "${UNINSTKEY}" "DisplayName" "${APPNAME}"
  WriteRegStr HKCU "${UNINSTKEY}" "DisplayVersion" "${APPVERSION}"
  WriteRegStr HKCU "${UNINSTKEY}" "Publisher" "${PUBLISHER}"
  WriteRegStr HKCU "${UNINSTKEY}" "DisplayIcon" "$INSTDIR\cas.ico"
  WriteRegStr HKCU "${UNINSTKEY}" "InstallLocation" "$INSTDIR"
  WriteRegStr HKCU "${UNINSTKEY}" "UninstallString" '"$INSTDIR\Uninstall ${APPNAME}.exe"'
  WriteRegStr HKCU "${UNINSTKEY}" "QuietUninstallString" '"$INSTDIR\Uninstall ${APPNAME}.exe" /S'
  WriteRegDWORD HKCU "${UNINSTKEY}" "NoModify" 1
  WriteRegDWORD HKCU "${UNINSTKEY}" "NoRepair" 1
  ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
  IntFmt $0 "0x%08X" $0
  WriteRegDWORD HKCU "${UNINSTKEY}" "EstimatedSize" "$0"
SectionEnd

; ---------------------------------------------------------------- uninstall
Section "Uninstall"
  Delete "$DESKTOP\${APPNAME}.lnk"
  Delete "$SMPROGRAMS\${APPNAME}.lnk"
  ; Only remove what the installer put there
  RMDir /r "$INSTDIR\runtime"
  RMDir /r "$INSTDIR\app"
  Delete "$INSTDIR\db.properties"
  Delete "$INSTDIR\cas.ico"
  Delete "$INSTDIR\Uninstall ${APPNAME}.exe"
  RMDir "$INSTDIR"
  DeleteRegKey HKCU "${UNINSTKEY}"
  DeleteRegKey HKCU "${APPKEY}"
SectionEnd

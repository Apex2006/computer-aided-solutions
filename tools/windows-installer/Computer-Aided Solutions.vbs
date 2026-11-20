' Starts Computer-Aided Solutions (no NetBeans or Java install needed).
' @author Luis Gabriel Santiago
Set fso = CreateObject("Scripting.FileSystemObject")
Set sh = CreateObject("WScript.Shell")
base = fso.GetParentFolderName(WScript.ScriptFullName)
sh.CurrentDirectory = base
sh.Run """" & base & "\runtime\bin\javaw.exe"" -jar """ & base & "\app\computer-aided-solutions.jar""", 1, False

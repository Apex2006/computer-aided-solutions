' Puts a "Computer-Aided Solutions" shortcut on the Desktop and in the Start menu.
' Run it again if you move this folder.
' @author Luis Gabriel Santiago
Set fso = CreateObject("Scripting.FileSystemObject")
Set sh = CreateObject("WScript.Shell")
base = fso.GetParentFolderName(WScript.ScriptFullName)
For Each place In Array(sh.SpecialFolders("Desktop"), sh.SpecialFolders("Programs"))
    Set lnk = sh.CreateShortcut(place & "\Computer-Aided Solutions.lnk")
    lnk.TargetPath = base & "\runtime\bin\javaw.exe"
    lnk.Arguments = "-jar """ & base & "\app\computer-aided-solutions.jar"""
    lnk.WorkingDirectory = base
    lnk.IconLocation = base & "\cas.ico,0"
    lnk.Description = "Repository of Computer-Aided Solutions for Common Everyday Problems"
    lnk.Save
Next
MsgBox "Done! Open Computer-Aided Solutions from your Desktop or the Start menu.", 64, "Computer-Aided Solutions"

Write-Output "JAVA_HOME=$env:JAVA_HOME"
$jj = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
if (Test-Path $jj) { Write-Output "FOUND_IN_JAVA_HOME:$jj" } else { Write-Output "Not in JAVA_HOME" }
$java = Get-Command java -ErrorAction SilentlyContinue
if ($java) {
  Write-Output "java: $($java.Source)"
  $javaDir = Split-Path $java.Source -Parent
  $jj2 = Join-Path $javaDir 'jpackage.exe'
  if (Test-Path $jj2) { Write-Output "FOUND_NEXT_TO_JAVA:$jj2" } else { Write-Output "Not next to java" }
} else { Write-Output "java not found" }
Get-ChildItem 'C:\Program Files','C:\Program Files (x86)' -Recurse -Filter jpackage.exe -ErrorAction SilentlyContinue | Select-Object FullName -First 5 | ForEach-Object { Write-Output "FOUND_IN_PROGRAM_FILES:$($_.FullName)" }
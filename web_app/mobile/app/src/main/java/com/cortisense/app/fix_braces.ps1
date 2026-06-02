$path = "c:\Users\venug\AndroidStudioProjects\CortiSense\app\src\main\java\com\cortisense\app\MainActivity.kt"
$content = Get-Content $path
$newContent = @()
for ($i = 0; $i -lt $content.Count; $i++) {
    $trimmed = $content[$i].Trim()
    if ($i -ge 5930 -and $trimmed -eq "}") { continue } # Skip stray braces at end
    if ($i -eq 1802 -and $trimmed -eq "") { continue } # Skip previously emptied line
    $newContent += $content[$i]
}
$newContent | Set-Content $path

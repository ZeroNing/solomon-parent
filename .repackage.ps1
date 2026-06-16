$ErrorActionPreference = 'Stop'
$root = 'C:\软件\project\solomon-parent'
$utf8 = New-Object System.Text.UTF8Encoding $false

# 模块映射：模块路径 -> 子包后缀 -> 是否需要移动文件
$modules = @(
    @{ path='solomon-mqtt-module\solomon-mqtt5';      suffix='v5';    move=$true },
    @{ path='solomon-mqtt-module\solomon-vertx-mqtt';  suffix='vertx'; move=$true },
    @{ path='solomon-mqtt-module\solomon-mica-mqtt';   suffix='mica';  move=$true },
    @{ path='solomon-mqtt-module\solomon-redis-mqtt';  suffix='redis'; move=$true },
    @{ path='solomon-mqtt-module\solomon-mqtt';        suffix='v3';    move=$false }
)
$subpkgs = @('config','consumer','profile','service','utils')

foreach ($mod in $modules) {
    $mpath = $mod.path
    $suffix = $mod.suffix
    $javaRoot = Join-Path $root "$mpath\src\main\java\com\steven\solomon"
    Write-Output "===== 处理模块: $mpath (子包: mqtt.$suffix) move=$($mod.move) ====="

    # 1. 移动文件（v3 已手动移过，跳过）
    if ($mod.move) {
        foreach ($sp in $subpkgs) {
            $srcDir = Join-Path $javaRoot $sp
            $destDir = Join-Path $javaRoot "mqtt\$suffix\$sp"
            if (Test-Path $srcDir) {
                $files = Get-ChildItem -Path $srcDir -Filter '*.java'
                if ($files) {
                    if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
                    foreach ($f in $files) {
                        Push-Location $root
                        $relSrc = $f.FullName.Substring($root.Length + 1)
                        $relDest = (Join-Path $destDir $f.Name).Substring($root.Length + 1)
                        & git mv $relSrc $relDest 2>&1 | Out-Null
                        Pop-Location
                    }
                    Write-Output "  迁移 $sp : $($files.Count) 文件"
                }
            }
        }
    }

    # 2. 更新该模块所有 java 文件的包声明和 import
    $allJava = Get-ChildItem -Path $javaRoot -Recurse -Filter '*.java' -ErrorAction SilentlyContinue
    foreach ($f in $allJava) {
        $content = [System.IO.File]::ReadAllText($f.FullName, $utf8)
        $newContent = $content
        foreach ($sp in $subpkgs) {
            $newContent = $newContent -replace "package com\.steven\.solomon\.$sp;", "package com.steven.solomon.mqtt.$suffix.$sp;"
            $newContent = $newContent -replace "import com\.steven\.solomon\.$sp\.", "import com.steven.solomon.mqtt.$suffix.$sp."
        }
        if ($newContent -ne $content) {
            [System.IO.File]::WriteAllText($f.FullName, $newContent, $utf8)
            Write-Output "  更新包声明: $($f.Name)"
        }
    }
}
Write-Output "===== 完成 ====="

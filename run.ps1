#!/usr/bin/env pwsh
Set-StrictMode -Version Latest
if ($args.Length -eq 0) { Write-Host "You must specify a script to run." }
else {
	$remaining = [String[]] ($args | Select-Object -Skip 1)
	java "$PSScriptRoot/scripts/$($args[0]).java" @remaining
}

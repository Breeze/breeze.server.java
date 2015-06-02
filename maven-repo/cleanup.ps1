# change maven-metadata-local.xml to maven-metadata.xml
Get-ChildItem -Recurse | Rename-Item -NewName {$_.name -replace "-local","" }

# remove _remote.repositories
Get-ChildItem -Filter "_remote.repositories" -Recurse | Remove-Item 

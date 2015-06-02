# To rebuild the deployable maven-repo, run this after doing a full build.

# Copy from machine repo to maven-repo directory
Copy-Item -Path $HOME\.m2\repository\com\breeze -Destination ..\maven-repo\com -Recurse

# change maven-metadata-local.xml to maven-metadata.xml
Get-ChildItem -Path ..\maven-repo\com\breeze -Recurse | Rename-Item -NewName {$_.name -replace "-local","" }

# remove _remote.repositories
Get-ChildItem -Path ..\maven-repo\com\breeze -Filter "_remote.repositories" -Recurse | Remove-Item 

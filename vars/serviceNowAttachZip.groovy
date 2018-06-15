zip zipFile: 'my-zip-file.zip', glob: '*.txt'
serviceNow_attachZip serviceNowConfiguration: [instance: 'exampledev'], credentialsId: 'jenkins-vault', serviceNowItem: [sysId: 'agsdh0wehosid9723h30h', filename: 'my-zip-file.zip'], vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']

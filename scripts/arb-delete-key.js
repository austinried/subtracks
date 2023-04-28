const path = require('path')
const fs = require('fs')
const process = require('process')

const key = process.argv[2]

const arbDir = path.join(__dirname, '../lib/l10n')

const arbFiles = fs.readdirSync(arbDir)
for (const file of arbFiles) {
  const json = JSON.parse(fs.readFileSync(path.join(arbDir, file), { encoding: 'utf8' }))

  delete json[key]
  delete json[`@${key}`]

  fs.writeFileSync(path.join(arbDir, file), JSON.stringify(json, null, 4))
}

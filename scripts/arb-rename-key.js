const path = require('path')
const fs = require('fs')
const process = require('process')

const from = process.argv[2]
const to = process.argv[3]

const arbDir = path.join(__dirname, '../lib/l10n')

const arbFiles = fs.readdirSync(arbDir)
for (const file of arbFiles) {
  const json = JSON.parse(fs.readFileSync(path.join(arbDir, file), { encoding: 'utf8' }))

  json[to] = json[from]
  json[`@${to}`] = json[`@${from}`]

  delete json[from]
  delete json[`@${from}`]

  fs.writeFileSync(path.join(arbDir, file), JSON.stringify(sortObj(json), null, 4))
}

function sortObj(obj) {
  return Object.keys(obj)
    .sort((a, b) => moveSymbol(a).localeCompare(moveSymbol(b)))
    .reduce(function (result, key) {
      result[key] = obj[key]
      return result
    }, {})
}

function moveSymbol(str) {
  if (str.startsWith('@')) {
    return `${str.substring(1)}@`
  }
  return str
}

const fs = require('fs');
const path = require('path');

fs.copyFile(
  path.join(__dirname, '../../node_modules/@tianmiantech/theme/src/theme.css'),
  path.join(__dirname, '../themeconfig/theme.css'),
  (err) => {
    if (err) throw err;
    console.log('node_modules/@tianmiantech/theme/src/theme.css was copied to themeconfig/theme.css');
  },
);

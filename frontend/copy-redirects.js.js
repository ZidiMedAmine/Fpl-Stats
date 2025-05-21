const fs = require('fs');
const path = require('path');

const src = path.join(__dirname, 'src', '_redirects');
const dest = path.join(__dirname, 'dist', 'fpl-stats-frontend', '_redirects');

fs.copyFile(src, dest, (err) => {
  if (err) {
    console.error('❌ Failed to copy _redirects file:', err);
    process.exit(1);
  } else {
    console.log('✅ _redirects file copied successfully');
  }
});

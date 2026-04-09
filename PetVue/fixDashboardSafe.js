import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const filePath = path.join(__dirname, 'src/views/institution/DashboardView.vue');

// Read file with proper encoding
let content = fs.readFileSync(filePath, { encoding: 'utf8' });

// Let's process the content by replacing each occurrence individually
// First, let's replace the revenue card
content = content.replace(
  '      &lt;div class="stat-card"&gt;\r\n        &lt;div class="stat-icon revenue"&gt;&lt;DollarSign :size="24" /&gt;&lt;/div&gt;',
  '      &lt;div class="stat-card" @click="router.push(\'/institution/reports\')"&gt;\r\n        &lt;div class="stat-icon revenue"&gt;&lt;DollarSign :size="24" /&gt;&lt;/div&gt;'
);

// Replace occupancy card
content = content.replace(
  '      &lt;div class="stat-card"&gt;\r\n        &lt;div class="stat-icon occupancy"&gt;&lt;Users :size="24" /&gt;&lt;/div&gt;',
  '      &lt;div class="stat-card" @click="router.push(\'/institution/orders\')"&gt;\r\n        &lt;div class="stat-icon occupancy"&gt;&lt;Users :size="24" /&gt;&lt;/div&gt;'
);

// Replace rating card
content = content.replace(
  '      &lt;div class="stat-card"&gt;\r\n        &lt;div class="stat-icon rating"&gt;&lt;Star :size="24" /&gt;&lt;/div&gt;',
  '      &lt;div class="stat-card" @click="router.push(\'/institution/reviews\')"&gt;\r\n        &lt;div class="stat-icon rating"&gt;&lt;Star :size="24" /&gt;&lt;/div&gt;'
);

// Write back the file with proper encoding
fs.writeFileSync(filePath, content, { encoding: 'utf8' });

console.log('Dashboard fixed successfully!');
console.log('Verify changes by checking the file...');

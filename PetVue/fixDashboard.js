import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const filePath = path.join(__dirname, 'src/views/institution/DashboardView.vue');

let content = fs.readFileSync(filePath, 'utf-8');

// Replace each stat-card without click handler
content = content.replace(
  '      &lt;div class="stat-card"&gt;\n        &lt;div class="stat-icon revenue"&gt;&lt;DollarSign :size="24" /&gt;&lt;/div&gt;',
  '      &lt;div class="stat-card" @click="router.push(\'/institution/reports\')"&gt;\n        &lt;div class="stat-icon revenue"&gt;&lt;DollarSign :size="24" /&gt;&lt;/div&gt;'
);

content = content.replace(
  '      &lt;div class="stat-card"&gt;\n        &lt;div class="stat-icon occupancy"&gt;&lt;Users :size="24" /&gt;&lt;/div&gt;',
  '      &lt;div class="stat-card" @click="router.push(\'/institution/orders\')"&gt;\n        &lt;div class="stat-icon occupancy"&gt;&lt;Users :size="24" /&gt;&lt;/div&gt;'
);

content = content.replace(
  '      &lt;div class="stat-card"&gt;\n        &lt;div class="stat-icon rating"&gt;&lt;Star :size="24" /&gt;&lt;/div&gt;',
  '      &lt;div class="stat-card" @click="router.push(\'/institution/reviews\')"&gt;\n        &lt;div class="stat-icon rating"&gt;&lt;Star :size="24" /&gt;&lt;/div&gt;'
);

fs.writeFileSync(filePath, content, 'utf-8');
console.log('Dashboard fixed successfully!');

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const filePath = path.join(__dirname, 'src/views/institution/DashboardView.vue');
let content = fs.readFileSync(filePath, 'utf-8');

// Find the lines around stat-card
const lines = content.split('\n');
for (let i = 575; i < 615; i++) {
  console.log(i + ': ' + JSON.stringify(lines[i]));
}

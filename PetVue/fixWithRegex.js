import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const filePath = path.join(__dirname, 'src/views/institution/DashboardView.vue');

let content = fs.readFileSync(filePath, 'utf8');

// Use regex to match and replace
// Match revenue card
content = content.replace(/<div class="stat-card">\s*<div class="stat-icon revenue">/, 
  `<div class="stat-card" @click="router.push('/institution/reports')">
        <div class="stat-icon revenue">`);

// Match occupancy card
content = content.replace(/<div class="stat-card">\s*<div class="stat-icon occupancy">/, 
  `<div class="stat-card" @click="router.push('/institution/orders')">
        <div class="stat-icon occupancy">`);

// Match rating card
content = content.replace(/<div class="stat-card">\s*<div class="stat-icon rating">/, 
  `<div class="stat-card" @click="router.push('/institution/reviews')">
        <div class="stat-icon rating">`);

fs.writeFileSync(filePath, content, 'utf8');

console.log('Successfully fixed with regex!');

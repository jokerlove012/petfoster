import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const filePath = path.join(__dirname, 'PetJava/src/main/java/com/pet/service/AdminService.java');

let content = fs.readFileSync(filePath, 'utf8');

// Use regex to replace the method
content = content.replace(
  /public Map<String, Object> getOrderDetail\(String id\) \{[\s\S]*?return toBookingVO\(booking\);[\s\S]*?\}/,
  `public Map<String, Object> getOrderDetail(String idOrOrderNumber) {
        Booking booking = bookingMapper.selectById(idOrOrderNumber);
        if (booking == null) {
            booking = bookingMapper.selectOne(new LambdaQueryWrapper<Booking>()
                    .eq(Booking::getOrderNumber, idOrOrderNumber));
        }
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        return toBookingVO(booking);
    }`
);

fs.writeFileSync(filePath, content, 'utf8');

console.log('AdminService.java updated successfully with regex!');

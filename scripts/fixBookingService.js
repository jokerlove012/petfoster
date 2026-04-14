import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const filePath = path.join(__dirname, 'PetJava/src/main/java/com/pet/service/BookingService.java');

let content = fs.readFileSync(filePath, 'utf8');

// Use regex to replace the getDetail method in BookingService
content = content.replace(
  /public Map<String, Object> getDetail\(String id\) \{[\s\S]*?return toBookingVO\(booking\);[\s\S]*?\}/,
  `public Map<String, Object> getDetail(String idOrOrderNumber) {
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

console.log('BookingService.java updated successfully!');

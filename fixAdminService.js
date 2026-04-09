import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const filePath = path.join(__dirname, 'PetJava/src/main/java/com/pet/service/AdminService.java');

let content = fs.readFileSync(filePath, 'utf8');

// Replace the getOrderDetail method
const oldMethod = `    public Map<String, Object> getOrderDetail(String id) {
        Booking booking = bookingMapper.selectById(id);
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        return toBookingVO(booking);
    }`;

const newMethod = `    public Map<String, Object> getOrderDetail(String idOrOrderNumber) {
        Booking booking = bookingMapper.selectById(idOrOrderNumber);
        if (booking == null) {
            booking = bookingMapper.selectOne(new LambdaQueryWrapper<Booking>()
                    .eq(Booking::getOrderNumber, idOrOrderNumber));
        }
        if (booking == null) {
            throw new RuntimeException("订单不存在");
        }
        return toBookingVO(booking);
    }`;

content = content.replace(oldMethod, newMethod);

fs.writeFileSync(filePath, content, 'utf8');

console.log('AdminService.java updated successfully!');

package fu.ce190737.room.common;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public final class Check {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "^[0-9+\\-()\\s]{7,20}$";

    private Check() {
    }

    // 1. Check điều kiện bất kỳ (sai -> ném 406, status 2)
    public static void isTrue(boolean condition) {
        if (!condition) throw ApiException.invalid();
    }

    // 2. Check khác null (dùng cho Enum, Object bắt buộc khi POST)
    public static <T> T notNull(T val) {
        if (val == null) throw ApiException.invalid();
        return val;
    }

    // 3. ID (Long): phải != null và > 0
    public static Long id(Long id) {
        if (id == null || id <= 0)
            throw ApiException.invalid();
        return id;
    }

    // 4. String / Name / Code: không rỗng và độ dài <= maxLen
    public static String str(String val, int maxLen) {
        if (val == null || val.isBlank() || val.length() > maxLen) {
            throw ApiException.invalid();
        }
        return val;
    }

    // 5. String có cả minLen và maxLen
    public static String str(String val, int minLen, int maxLen) {
        if (val == null || val.isBlank() || val.length() < minLen || val.length() > maxLen) {
            throw ApiException.invalid();
        }
        return val;
    }

    // 6. Email: không rỗng, <= maxLen, đúng định dạng email
    public static String email(String val, int maxLen) {
        str(val, maxLen);
        if (!val.matches(EMAIL_REGEX))
            throw ApiException.invalid();
        return val;
    }

    // 7. Phone: không rỗng, <= maxLen (nếu đề chỉ check maxLen thì dùng Check.str, nếu bắt số thì dùng hàm này)
    public static String phone(String val, int maxLen) {
        str(val, maxLen);
        if (!val.matches(PHONE_REGEX))
            throw ApiException.invalid();
        return val;
    }

    // 8. Số nguyên (Integer) >= min (VD: floor >= 1, numberOfGuests >= 1)
    public static Integer intMin(Integer val, int min) {
        if (val == null || val < min)
            throw ApiException.invalid();
        return val;
    }

    // 9. Số nguyên (Integer) nằm trong đoạn [min, max] (VD: capacity từ 1..10)
    public static Integer intRange(Integer val, int min, int max) {
        if (val == null || val < min || val > max)
            throw ApiException.invalid();
        return val;
    }

    // 10. Tiền / Giá (BigDecimal) > 0 (VD: pricePerNight > 0)
    public static BigDecimal decimalGtZero(BigDecimal val) {
        if (val == null || val.compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.invalid();
        }
        return val;
    }

    // 11. Tiền / Tổng tiền (BigDecimal) >= 0 (VD: totalAmount >= 0, discount >= 0)
    public static BigDecimal decimalMinZero(BigDecimal val) {
        if (val == null || val.compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.invalid();
        }
        return val;
    }

    // 12. Ngày bắt đầu - Ngày kết thúc (VD: checkOutDate phải sau checkInDate)
    public static void dateAfter(LocalDate start, LocalDate end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw ApiException.invalid();
        }
    }

    public static void dateAfter(Date start, Date end) {
        if (start == null || end == null || !end.after(start)) {
            throw ApiException.invalid("Date error");
        }
    }

    // 13. Ngày không được ở quá khứ / phải ở quá khứ (VD: ngày sinh, ngày hẹn)
    public static LocalDate dateNotPast(LocalDate date) {
        if (date == null || date.isBefore(LocalDate.now()))
            throw ApiException.invalid();
        return date;
    }

    public static LocalDate datePast(LocalDate date) {
        if (date == null || !date.isBefore(LocalDate.now()))
            throw ApiException.invalid();
        return date;
    }

    // 14. Giờ bắt đầu - Giờ kết thúc (LocalTime / LocalDateTime)
    public static void timeAfter(LocalTime start, LocalTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw ApiException.invalid();
        }
    }


    public static void dateTimeAfter(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw ApiException.invalid();
        }
    }

    // 15. Parse String query param sang Enum (nếu null/blank -> trả về null để không lọc; nếu sai -> ném 406)
    public static <E extends Enum<E>> E enumParam(Class<E> enumClass, String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Enum.valueOf(enumClass, val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.invalid();
        }
    }

    // 16. Validate phân trang (page >= 0, 1 <= size <= 100)
    public static void page(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw ApiException.invalid();
        }
    }

    // 17. Parse String sang LocalDate (chấp nhận cả dd/MM/yyyy và yyyy-MM-dd)
    public static LocalDate parseDate(String val) {
        if (val == null || val.isBlank())
            throw ApiException.invalid();
        String s = val.trim();
        try {
            return s.contains("/")
                    ? LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw ApiException.invalid();
        }
    }

    private static LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

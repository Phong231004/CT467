USE ComputerStore;
select * from users;
SET SQL_SAFE_UPDATES = 0;
-- NHÓM 01: THÊM SỬA XÓA --
-- insert --
DELIMITER //
CREATE PROCEDURE generic_insert(
    IN tableName VARCHAR(50),
    IN columns VARCHAR(200),
    IN value VARCHAR(200)
)
BEGIN
    -- Determine if a date column is required and missing
    DECLARE dateColumn VARCHAR(50);
    SET dateColumn = '';

    -- Identify the date column based on the table name
    IF tableName = 'PhieuNhap' THEN
        SET dateColumn = 'NgayNhap';
    ELSEIF tableName = 'PhieuXuat' THEN
        SET dateColumn = 'NgayXuat';
    END IF;

    -- Add the date column and CURDATE() if missing from columns
    IF dateColumn != '' AND FIND_IN_SET(dateColumn, columns) = 0 THEN
        SET columns = CONCAT(columns, ',', dateColumn);
        SET value = CONCAT(value, ', CURDATE()');
    END IF;

    -- Build and execute the SQL insert statement
    SET @sql = CONCAT('INSERT INTO ', tableName, ' (', columns, ') VALUES (', value, ')');
    
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END//
DELIMITER ;
drop procedure generic_insert;
CALL generic_insert('PhieuNhap', 'MaNhanVien, MaNhaCungCap', "260, 2");
-- -- 
DELIMITER //
CREATE TRIGGER set_NgayNhap_before_insert
BEFORE INSERT ON PhieuNhap
FOR EACH ROW
BEGIN
    IF NEW.NgayNhap IS NULL THEN
        SET NEW.NgayNhap = CURDATE();
    END IF;
END//
DELIMITER ;
-- -- 
DELIMITER //
CREATE TRIGGER set_NgayXuat_before_insert
BEFORE INSERT ON PhieuXuat
FOR EACH ROW
BEGIN
    IF NEW.NgayXuat IS NULL THEN
        SET NEW.NgayXuat = CURDATE();
    END IF;
END//
DELIMITER ;

-- update --
DELIMITER //
CREATE PROCEDURE generic_update(
    IN tableName VARCHAR(50),
    IN updates VARCHAR(200),
    IN conditions VARCHAR(100)
)
BEGIN
    -- Build and execute the SQL update statement
    SET @sql = CONCAT('UPDATE ', tableName, ' SET ', updates, ' WHERE ', conditions);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Display the updated row
    SET @select_sql = CONCAT('SELECT * FROM ', tableName, ' WHERE ', conditions);
    PREPARE stmt FROM @select_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

drop PROCEDURE generic_update;
-- This will raise an error
CALL generic_update('Users', 'password="newpassword"', 'user_id=1');

-- This will execute normally
CALL generic_update('PhieuNhap', 'MaNhanVien = 2, MaNhaCungCap = 2', 'MaPhieuNhap = 10');
select * from PhieuNhap;
-- delete --
DELIMITER //
CREATE PROCEDURE generic_delete(
    IN tableName VARCHAR(50),
    IN conditions VARCHAR(100)
)
BEGIN
    -- Prevent deletion from the Users table
    IF tableName = 'Users' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Deletion from the Users table is not allowed.';
    ELSE
        -- Build and execute the delete statement for other tables
        SET @sql = CONCAT('DELETE FROM ', tableName, ' WHERE ', conditions);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;
drop  PROCEDURE generic_delete;
CALL generic_delete('Users', 'user_id = 1');
-- Error: Deletion from the Users table is not allowed.
select * from chitietphieunhap;
CALL generic_delete('chitietphieunhap', 'MaPhieuNhap = 134');
select * from chitietphieunhap;
-- NHÓM 02: TÌM KIẾM, SẮP XẾP ---
-- 1. Tìm kiếm thiết bị theo tên

DELIMITER //
CREATE PROCEDURE TimThietBiTheoTen(IN timTen VARCHAR(100))
BEGIN
     SELECT 
        th.MaThietBi,
        th.TenThietBi,
        th.ThongSoKT,
        th.GiaThanh,
        COALESCE(SUM(ctnp.SoLuong), 0) AS SoLuongNhap,
        COALESCE(SUM(ctpx.SoLuong), 0) AS SoLuongXuat,
        COALESCE(SUM(ctnp.SoLuong), 0) - COALESCE(SUM(ctpx.SoLuong), 0) AS SoLuong
    FROM 
        ThietBi th
    LEFT JOIN 
        ChiTietPhieuNhap ctnp ON th.MaThietBi = ctnp.MaThietBi
    LEFT JOIN 
        ChiTietPhieuXuat ctpx ON th.MaThietBi = ctpx.MaThietBi
	WHERE TenThietBi LIKE CONCAT('%', timTen, '%')
    GROUP BY 
        th.MaThietBi, th.TenThietBi;
END //
DELIMITER ;
drop procedure TimThietBiTheoTen;
CALL TimThietBiTheoTen('acer');

select * from nhasanxuat;
-- 2. Lọc sản phầm theo nhà sản xuất
DELIMITER //
CREATE PROCEDURE LocThietBiTheoNSX(IN idNSX INT)
BEGIN
    SELECT MaThietBi, TenThietBi, ThongSoKT, GiaThanh, SoLuong
    FROM ThietBi
    WHERE MaNSX = idNSX;
END //
DELIMITER ;
CALL LocThietBiTheoNSX(2); 

-- 3. Sắp xếp sản phẩm theo các tiêu chí (giá tăng dần, giá giảm dần, bán chạy nhất)

-- gía tăng dần
CREATE VIEW ThietBiGiaTangDan AS
SELECT T.MaThietBi, T.TenThietBi, T.GiaThanh, T.SoLuong, NSX.TenNSX, NCC.TenNhaCungCap
FROM ThietBi T
JOIN NhaSanXuat NSX ON T.MaNSX = NSX.MaNSX
JOIN NhaCungCap NCC ON T.MaNhaCungCap = NCC.MaNhaCungCap
ORDER BY T.GiaThanh ASC;

SELECT * FROM ThietBiGiaTangDan;
drop VIEW ThietBiGiaTangDan;

-- giá giảm dần

CREATE VIEW ThietBiGiaGiamDan AS
SELECT T.MaThietBi, T.TenThietBi, T.GiaThanh, T.SoLuong, NSX.TenNSX, NCC.TenNhaCungCap
FROM ThietBi T
JOIN NhaSanXuat NSX ON T.MaNSX = NSX.MaNSX
JOIN NhaCungCap NCC ON T.MaNhaCungCap = NCC.MaNhaCungCap
ORDER BY T.GiaThanh DESC;

SELECT * FROM ThietBiGiaGiamDan;


-- bán chạy nhất

CREATE VIEW ThietBiBanChayNhat AS
SELECT T.MaThietBi, T.TenThietBi, T.GiaThanh, T.SoLuong, NSX.TenNSX, NCC.TenNhaCungCap,
       SUM(CPX.SoLuong) AS TongSoLuongBan
FROM ThietBi T
JOIN ChiTietPhieuXuat CPX ON T.MaThietBi = CPX.MaThietBi
JOIN PhieuXuat PX ON CPX.MaPhieuXuat = PX.MaPhieuXuat
JOIN NhaSanXuat NSX ON T.MaNSX = NSX.MaNSX
JOIN NhaCungCap NCC ON T.MaNhaCungCap = NCC.MaNhaCungCap
GROUP BY T.MaThietBi, T.TenThietBi, T.GiaThanh, T.SoLuong, NSX.TenNSX, NCC.TenNhaCungCap
ORDER BY TongSoLuongBan DESC;

SELECT * FROM ThietBiBanChayNhat;

-- NHÓM 04: QUẢN LÝ KHO ---
-- Hàm tính tổng doanh thu(Phiếu xuất) theo năm --
DELIMITER //
CREATE FUNCTION TongDoanhThuTheoNam (Nam INT)
RETURNS DECIMAL(25,2)
DETERMINISTIC
BEGIN
    DECLARE TongDoanhThu DECIMAL(25,2);
    SELECT SUM(CTPX.DonGia * CTPX.SoLuong) INTO TongDoanhThu
    FROM ChiTietPhieuXuat AS CTPX
    JOIN PhieuXuat AS PX ON CTPX.MaPhieuXuat = PX.MaPhieuXuat
    WHERE YEAR(PX.NgayXuat) = Nam;
    RETURN TongDoanhThu;
END //
DELIMITER ;
select TongDoanhThuTheoNam(2024) as TongDoanhThuTheoNam;


-- Bang doanh thu theo thang trong nam--
DELIMITER //
CREATE PROCEDURE BangDoanhThuTheoThangTrongNam()
BEGIN
	DECLARE Nam INT DEFAULT 2023;
	DROP TEMPORARY TABLE IF EXISTS TempDoanhThu;
    -- Tạo bảng tạm để lưu trữ doanh thu theo từng tháng
    CREATE TEMPORARY TABLE IF NOT EXISTS TempDoanhThu (
        Thang INT,
        TongDoanhThu DECIMAL(50, 2) DEFAULT 0
    );
    -- Chèn tất cả các tháng từ 1 đến 12 vào bảng tạm
    INSERT INTO TempDoanhThu (Thang) 
    SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL 
           SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL 
           SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL 
           SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12;
    -- Tính tổng doanh thu từ đầu năm đến tháng hiện tại
    UPDATE TempDoanhThu td
    JOIN (
        SELECT MONTH(PHIEUXUAT.NgayXuat) AS Thang,
               SUM(COALESCE(CHITIETPHIEUXUAT.DonGia*CHITIETPHIEUXUAT.SoLuong, 0)) AS TongDoanhThu
        FROM PHIEUXUAT
        JOIN CHITIETPHIEUXUAT ON PHIEUXUAT.MaPhieuXuat = CHITIETPHIEUXUAT.MaPhieuXuat
        WHERE YEAR(PHIEUXUAT.NgayXuat) = Nam
          AND MONTH(PHIEUXUAT.NgayXuat) <= MONTH(CURRENT_DATE)  -- Chỉ tính đến tháng hiện tại
        GROUP BY MONTH(PHIEUXUAT.NgayXuat)
    ) AS doanhthu ON td.Thang = doanhthu.Thang
    SET td.TongDoanhThu = doanhthu.TongDoanhThu;
    -- Trả về bảng doanh thu theo tháng
    SELECT * FROM TempDoanhThu ORDER BY Thang;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS BangDoanhThuTheoThangTrongNam;
SET SQL_SAFE_UPDATES = 0;
CALL BangDoanhThuTheoThangTrongNam();


-- Bảng tổng doanh thu qua các năm (từ 2018) --
DELIMITER //
CREATE PROCEDURE BangDoanhThuTheoNam()
BEGIN
    DECLARE TongDoanhThu DECIMAL(25,2);
    DECLARE nam INT DEFAULT 2018;
    DECLARE nam_hien_tai INT DEFAULT YEAR(CURDATE());
    -- Tạo bảng tạm để lưu trữ doanh thu theo năm
    DROP TEMPORARY TABLE IF EXISTS DoanhThuTheoNam;
    CREATE TEMPORARY TABLE DoanhThuTheoNam (
        Nam INT,
        TongDoanhThu DECIMAL(25,2)
    );
    -- Lặp từ năm 2006 đến năm hiện tại và chèn doanh thu vào bảng tạm
    WHILE nam <= nam_hien_tai DO
        SET TongDoanhThu = TongDoanhThuTheoNam(nam);
        INSERT INTO DoanhThuTheoNam (Nam, TongDoanhThu)
        VALUES (nam, IFNULL(TongDoanhThu, 0));
        SET nam = nam + 1;
    END WHILE;
    -- Trả về bảng tạm
    SELECT * FROM DoanhThuTheoNam;
END //
DELIMITER ;
call BangDoanhThuTheoNam();


-- Doanh thu các ngày trong tuần --
DELIMITER //

CREATE PROCEDURE TinhDoanhThuTheoNgay()
BEGIN
    DECLARE v_current_date DATE;
    DECLARE today DATE DEFAULT CURDATE();
    DECLARE first_day_of_month DATE;
    DECLARE last_day_of_month DATE;

    -- Xóa bảng nếu đã tồn tại
    DROP TABLE IF EXISTS DoanhThuTheoNgay;

    -- Tạo bảng mới để lưu kết quả doanh thu theo từng ngày
    CREATE TABLE DoanhThuTheoNgay (
        Ngay DATE PRIMARY KEY,
        TongDoanhThu DECIMAL(25, 2) NOT NULL DEFAULT 0
    );

    -- Lấy ngày đầu tiên và ngày cuối cùng của tháng hiện tại
    SET first_day_of_month = DATE_FORMAT(today, '%Y-%m-01');  -- Ngày đầu tháng
    SET last_day_of_month = LAST_DAY(today);  -- Ngày cuối tháng

    -- Lặp qua tất cả các ngày trong tháng hiện tại
    SET v_current_date = first_day_of_month;

    WHILE v_current_date <= last_day_of_month DO
        -- Tính tổng doanh thu cho ngày hiện tại
        INSERT INTO DoanhThuTheoNgay (Ngay, TongDoanhThu)
        SELECT v_current_date, IFNULL(SUM(c.DonGia * c.SoLuong), 0)
        FROM CHITIETPHIEUXUAT c
        JOIN PHIEUXUAT p ON c.MaPhieuXuat = p.MaPhieuXuat
        WHERE p.NgayXuat = v_current_date; -- So sánh trực tiếp với ngày

        -- Chuyển đến ngày tiếp theo
        SET v_current_date = DATE_ADD(v_current_date, INTERVAL 1 DAY);
    END WHILE;

    -- Trả về kết quả
    SELECT * FROM DoanhThuTheoNgay;

END //

DELIMITER ;


drop procedure TinhDoanhThuTheoNgay;
call TinhDoanhThuTheoNgay();
select * from nhacungcap

-- Doanh thu theo tung san pham trong năm --
DELIMITER //

CREATE PROCEDURE TinhTongDoanhThuTheoSanPham()
BEGIN
    -- Drop the temporary table if it exists
    DROP TEMPORARY TABLE IF EXISTS TongDoanhThuSanPham;

    -- Create a temporary table to hold the results
    CREATE TEMPORARY TABLE TongDoanhThuSanPham (
        MaThietBi VARCHAR(15),
        TenThietBi VARCHAR(255),
        TongDoanhThu DECIMAL(50, 2) DEFAULT 0,  -- Reasonable size for revenue
        PRIMARY KEY (MaThietBi)
    );

    -- Insert calculated total revenue for each product in 2024 into the temporary table
    INSERT INTO TongDoanhThuSanPham (MaThietBi, TenThietBi, TongDoanhThu)
    SELECT 
        t.MaThietBi,
        t.TenThietBi,
        IFNULL(SUM(c.DonGia * c.SoLuong), 0) AS TongDoanhThu  -- Calculate total revenue, defaulting to 0
    FROM 
        THIETBI t
    LEFT JOIN 
        CHITIETPHIEUXUAT c ON t.MaThietBi = c.MaThietBi
    LEFT JOIN 
        PHIEUXUAT p ON c.MaPhieuXuat = p.MaPhieuXuat
    WHERE 
        YEAR(p.NgayXuat) = 2024  -- Only consider sales in 2024
    GROUP BY 
        t.MaThietBi, t.TenThietBi;

    -- Select results from the temporary table for today
    SELECT 
        ts.MaThietBi,
        ts.TenThietBi,
        IFNULL(SUM(c.DonGia * c.SoLuong), 0) AS TongDoanhThu
    FROM 
        TongDoanhThuSanPham ts
    LEFT JOIN 
        CHITIETPHIEUXUAT c ON ts.MaThietBi = c.MaThietBi
    LEFT JOIN 
        PHIEUXUAT p ON c.MaPhieuXuat = p.MaPhieuXuat
    WHERE 
        YEAR(p.NgayXuat) = 2024  -- Ensure it's in the year 2024
    GROUP BY 
        ts.MaThietBi, ts.TenThietBi;

    -- Optionally: Drop the temporary table if no longer needed
    DROP TEMPORARY TABLE IF EXISTS TongDoanhThuSanPham;
END //

DELIMITER ;


drop PROCEDURE TinhTongDoanhThuTheoSanPham;
call TinhTongDoanhThuTheoSanPham();


-- Doanh Thu theo thang trong nam--
DELIMITER //
CREATE FUNCTION DoanhThuTheoThang(Thang INT, Nam INT)
RETURNS DECIMAL(50, 2)
DETERMINISTIC
BEGIN
    DECLARE totalRevenue DECIMAL(50, 2);

    SELECT SUM(CHITIETPHIEUXUAT.Dongia*CHITIETPHIEUXUAT.SoLuong) INTO totalRevenue
    FROM CHITIETPHIEUXUAT
    JOIN PHIEUXUAT ON CHITIETPHIEUXUAT.MaPhieuXuat = PHIEUXUAT.MaPhieuXuat
    WHERE MONTH(PHIEUXUAT.NgayXuat) = Thang AND YEAR(PHIEUXUAT.NgayXuat) = Nam;
    RETURN IFNULL(totalRevenue, 0);
END //
DELIMITER ;
drop function DoanhThuTheoThang;
select DoanhThuTheoThang(1,2024) as DoanhThuTheoThang;



-- Nhập hàng theo năm --
DELIMITER //
CREATE FUNCTION NhapHangTheoNam (Nam INT)
RETURNS DECIMAL(25,2)
DETERMINISTIC
BEGIN
    DECLARE TongNhap DECIMAL(25,2);
    SELECT SUM(CTPN.Dongia*CTPN.SoLuong) INTO TongNhap
    FROM CHITIETPHIEUNHAP AS CTPN
    JOIN PHIEUNHAP AS PN ON CTPN.MaPhieuNhap = PN.MaPhieuNhap
    WHERE YEAR(PN.NgayNhap) = Nam;
    RETURN TongNhap;
END //
DELIMITER ;
drop function NhapHangTheoNam; 
select NhapHangTheoNam(2024) as TongNhapTheoNam;


-- Bảng nhap theo nam --
DELIMITER //
CREATE PROCEDURE BangNhapTheoNam()
BEGIN
    DECLARE TongNhap DECIMAL(25,2);
    DECLARE nam INT DEFAULT 2018;
    DECLARE nam_hien_tai INT DEFAULT YEAR(CURDATE());
    -- Tạo bảng tạm để lưu trữ nhap hang theo năm
    DROP TEMPORARY TABLE IF EXISTS NhapTheoNam;
    CREATE TEMPORARY TABLE NhapThuTheoNam (
        Nam INT,
        TongNhap DECIMAL(25,2)
    );
    -- Lặp từ năm 1990 đến năm 2005 và chèn doanh thu vào bảng tạm
    WHILE nam <= nam_hien_tai DO
        SET TongNhap = NhapHangTheoNam(nam);
        INSERT INTO NhapThuTheoNam (Nam, TongNhap)
        VALUES (nam, IFNULL(TongNhap, 0));
        SET nam = nam + 1;
    END WHILE;
    -- Trả về bảng tạm
    SELECT * FROM NhapThuTheoNam;
END //
DELIMITER ;
call BangNhapTheoNam();


-- Nhập theo tháng trong năm --
DELIMITER //
CREATE FUNCTION NhapHangTheoThangTrongNam(Thang INT, Nam INT) 
RETURNS DECIMAL(50,2)
READS SQL DATA
BEGIN
    DECLARE nhaphang DECIMAL(50, 2);
    SELECT SUM(ctpn.DonGia*ctpn.SoLuong) INTO nhaphang
    FROM CHITIETPHIEUNHAP AS ctpn
    JOIN PHIEUNHAP AS pn ON ctpn.MaPhieuNhap = pn.MaPhieuNhap
    WHERE YEAR(pn.NgayNhap) = Nam AND MONTH(pn.NgayNhap) = Thang;
    RETURN COALESCE(nhaphang, 0);  -- Trả về 0 nếu không có doanh thu
END //
DELIMITER ;
select NhapHangTheoThangTrongNam(1,2024) as NhapHangTheoThangTrongNam;


-- Bảng nhập hàng tháng trong năm
DELIMITER //
CREATE PROCEDURE BangNhapTheoThangTrongNam(IN Nam INT)
BEGIN
	DROP TEMPORARY TABLE IF EXISTS TempNhap;
    -- Tạo bảng tạm để lưu trữ doanh thu theo từng tháng
    CREATE TEMPORARY TABLE IF NOT EXISTS TempNhap (
        Thang INT,
        TongNhapHang DECIMAL(50, 2) DEFAULT 0
    );

    -- Chèn tất cả các tháng từ 1 đến 12 vào bảng tạm
    INSERT INTO TempNhap (Thang) 
    VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10), (11), (12);

    -- Tính tổng doanh thu từ đầu năm đến tháng hiện tại
    UPDATE TempNhap AS nh
    JOIN (
        SELECT MONTH(PN.NgayNhap) AS Thang,
               SUM(IFNULL(CTPN.DonGia*CTPN.SoLuong, 0)) AS TongNhapHang
        FROM PHIEUNHAP PN
        JOIN CHITIETPHIEUNHAP CTPN ON PN.MaPhieuNhap = CTPN.MaPhieuNhap
        WHERE YEAR(PN.NgayNhap) = Nam
          AND MONTH(PN.NgayNhap) <= MONTH(CURRENT_DATE)  -- Chỉ tính đến tháng hiện tại
        GROUP BY MONTH(PN.NgayNhap)
    ) AS nhaphang ON nh.Thang = nhaphang.Thang
    SET nh.TongNhapHang = nhaphang.TongNhapHang;
    -- Trả về bảng doanh thu theo tháng, có sắp xếp theo tháng
    SELECT * FROM TempNhap ORDER BY Thang;
    -- Xóa bảng tạm
    DROP TEMPORARY TABLE IF EXISTS TempNhap;
END //
DELIMITER ;

drop procedure BangNhapTheoThangTrongNam;
SET SQL_SAFE_UPDATES = 0;
call BangNhapTheoThangTrongNam(2024);



-- Nhap hang theo tung san pham trong 1 năm --
DELIMITER //
CREATE PROCEDURE TinhTongNhapHangTheoSanPham()
BEGIN
	-- Drop the temporary table
    DROP TEMPORARY TABLE IF EXISTS TongNhapHangSanPham;
    -- Create a temporary table to hold the results
    CREATE TEMPORARY TABLE IF NOT EXISTS TongNhapHangSanPham (
        MaThietBi VARCHAR(15),
        TenThietBi VARCHAR(255),
        TongNhapHang DECIMAL(50, 2) DEFAULT 0,  -- Use a reasonable size
        PRIMARY KEY (MaThietBi)
    );

    -- Insert calculated revenue into the temporary table
    INSERT INTO TongNhapHangSanPham (MaThietBi, TenThietBi, TongNhapHang)
    SELECT 
        t.MaThietBi,
        t.TenThietBi,
        IFNULL(SUM(c.DonGia*c.SoLuong), 0) AS TongNhapHang  -- Calculate total revenue, defaulting to 0
    FROM 
        THIETBI t
    LEFT JOIN 
        CHITIETPHIEUNHAP c ON t.MaThietBi = c.MaThietBi
    LEFT JOIN 
        PHIEUNHAP p ON c.MaPhieuNhap = p.MaPhieuNhap
    GROUP BY 
        t.MaThietBi, t.TenThietBi;

    -- Select results from the temporary table for today
    SELECT * from TongNhapHangSanPham;
END //
DELIMITER ;

drop PROCEDURE TinhTongNhapHangTheoSanPham;
call TinhTongNhapHangTheoSanPham();



-- Nhập hàng theo nhà cung cấp --
DELIMITER //
CREATE PROCEDURE TinhTienNhapTheoNhaCungCap()
BEGIN
    -- Tạo bảng tạm thời chứa tổng tiền nhập của các nhà cung cấp
    CREATE TEMPORARY TABLE IF NOT EXISTS TongTienNhapNhaCungCap (
        MaNhaCungCap INT,
        TenNhaCungCap VARCHAR(100),
        TongTienNhap DECIMAL(10, 2) DEFAULT 0
    );

    -- Chèn dữ liệu vào bảng tạm thời
    INSERT INTO TongTienNhapNhaCungCap (MaNhaCungCap, TenNhaCungCap, TongTienNhap)
    SELECT 
        n.MaNhaCungCap,
        n.TenNhaCungCap,
        IFNULL(SUM(c.DonGia*c.SoLuong), 0) AS TongTienNhap
    FROM 
        NhaCungCap n
    LEFT JOIN 
        PhieuNhap p ON n.MaNhaCungCap = p.MaNhaCungCap
    LEFT JOIN 
        ChiTietPhieuNhap c ON p.MaPhieuNhap = c.MaPhieuNhap
    GROUP BY 
        n.MaNhaCungCap, n.TenNhaCungCap;

    -- Trả về bảng kết quả
    SELECT * FROM TongTienNhapNhaCungCap ORDER BY TongTienNhap DESC;

    -- Xóa bảng tạm thời
    DROP TEMPORARY TABLE IF EXISTS TongTienNhapNhaCungCap;
END //

DELIMITER ;
drop procedure TinhTienNhapTheoNhaCungCap;
CALL TinhTienNhapTheoNhaCungCap();

select * from ThietBi;



-- Nhap Hang các thang trong nam --
DELIMITER //
CREATE PROCEDURE TinhNhapHangTheoThang()
BEGIN
    DECLARE v_current_month DATE;
    DECLARE today DATE DEFAULT CURDATE();

    -- Xóa bảng nếu đã tồn tại
    DROP TABLE IF EXISTS NhapHangTheoThang;

    -- Tạo bảng mới để lưu dữ liệu nhập hàng theo tháng, với ThangNam là ngày đầu tiên của mỗi tháng
    CREATE TABLE NhapHangTheoThang (
        ThangNam DATE PRIMARY KEY, -- Lưu ngày đầu tiên của tháng
        TongNhapHang DECIMAL(25, 2) NOT NULL DEFAULT 0
    );

    -- Khởi tạo biến tháng hiện tại là ngày đầu tiên của tháng cách đây 11 tháng
    SET v_current_month = DATE_SUB(LAST_DAY(today), INTERVAL 11 MONTH);
    SET v_current_month = DATE_FORMAT(v_current_month, '%Y-%m-01'); -- Lấy ngày đầu tiên của tháng

    -- Lặp qua 12 tháng gần nhất
    WHILE v_current_month <= LAST_DAY(today) DO
        -- Tính tổng nhập hàng cho tháng hiện tại và lưu vào ngày đầu tiên của tháng đó
        INSERT INTO NhapHangTheoThang (ThangNam, TongNhapHang)
        SELECT v_current_month, IFNULL(SUM(c.DonGia * c.SoLuong), 0)
        FROM CHITIETPHIEUNHAP c
        JOIN PHIEUNHAP p ON c.MaPhieuNhap = p.MaPhieuNhap
        WHERE DATE_FORMAT(p.NgayNhap, '%Y-%m') = DATE_FORMAT(v_current_month, '%Y-%m');

        -- Chuyển đến tháng tiếp theo
        SET v_current_month = DATE_ADD(v_current_month, INTERVAL 1 MONTH);
    END WHILE;

    -- Trả về dữ liệu
    SELECT * FROM NhapHangTheoThang;
END //
DELIMITER ;
-- Gọi thủ tục để kiểm tra kết quả
drop procedure TinhNhapHangTheoThang;
CALL TinhNhapHangTheoThang();





-- Bảng lợi nhuận --
DELIMITER //
CREATE PROCEDURE BangLoiNhuan()
BEGIN
    DECLARE Nam INT DEFAULT 2018; -- Năm bắt đầu
    DECLARE NamCuoi INT DEFAULT YEAR(CURDATE()); -- Năm hiện tại
    -- Tạo bảng tạm để lưu lợi nhuận
    CREATE TEMPORARY TABLE IF NOT EXISTS BangLoiNhuan (
        Nam INT PRIMARY KEY,
        LoiNhuan DECIMAL(25,2)
    );
    -- Xóa dữ liệu cũ trong bảng tạm
    DELETE FROM BangLoiNhuan;
    -- Tính toán lợi nhuận cho từng năm
    WHILE Nam <= NamCuoi DO
        INSERT INTO BangLoiNhuan (Nam, LoiNhuan)
        VALUES (Nam, LoiNhuanTheoNam(Nam));
        SET Nam = Nam + 1; -- Tăng năm lên
    END WHILE;
    -- Hiển thị bảng lợi nhuận
    SELECT * FROM BangLoiNhuan;
    -- Xóa bảng tạm sau khi hiển thị
    DROP TEMPORARY TABLE IF EXISTS BangLoiNhuan;
END //
DELIMITER ;
drop procedure BangLoiNhuan;
SET SQL_SAFE_UPDATES = 0;
call BangLoiNhuan();


-- Lợi nhuận theo tháng --
DELIMITER //
CREATE FUNCTION LoiNhuanTheoThang(Thang INT, Nam INT)
RETURNS DECIMAL(25,2)
DETERMINISTIC
BEGIN
    DECLARE LoiNhuanThang DECIMAL(25,2);
    SET LoiNhuanThang = COALESCE(DoanhThuTheoThang(Thang, Nam), 0) - COALESCE(NhapHangTheoThangTrongNam(Thang, Nam), 0);
    RETURN LoiNhuanThang;
END //
DELIMITER ;
drop function LoiNhuanTheoThang;
select LoiNhuanTheoThang(1,2024) as LoiNhuanTheoThang;


-- Bảng lợi nhuận tháng trong năm --
DELIMITER //
CREATE PROCEDURE BangLoiNhuanTheoThangTrongNam()
BEGIN
	DECLARE Nam INT DEFAULT 2023;
    DECLARE ThangHienTai INT;
    SET ThangHienTai = IF(Nam = YEAR(CURRENT_DATE), MONTH(CURRENT_DATE), 12);
    -- Tạo bảng tạm để lưu trữ lợi nhuận theo tháng
    CREATE TEMPORARY TABLE IF NOT EXISTS TempLoiNhuan (
        Thang INT,
        LoiNhuan DECIMAL(25, 2) DEFAULT 0
    );
    -- Chèn các tháng từ 1 đến ThangHienTai vào bảng tạm
    INSERT INTO TempLoiNhuan (Thang)
    SELECT i FROM (SELECT 1 AS i UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6
                   UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12) AS Months
    WHERE i <= ThangHienTai;
    -- Cập nhật lợi nhuận cho từng tháng trong năm
    UPDATE TempLoiNhuan AS ln
    SET ln.LoiNhuan = LoiNhuanTheoThang(ln.Thang, Nam);
    -- Trả về bảng lợi nhuận theo tháng
    SELECT * FROM TempLoiNhuan ORDER BY Thang;
    -- Xóa bảng tạm sau khi hoàn tất
    DROP TEMPORARY TABLE IF EXISTS TempLoiNhuan;
END //
DELIMITER ;
drop procedure BangLoiNhuanTheoThangTrongNam;
call BangLoiNhuanTheoThangTrongNam();


-- lợi nhuận tổng mỗi sản phẩm --
DELIMITER //
CREATE PROCEDURE ThongKeSanPhamDaXuat()
BEGIN
    -- Tạo bảng tạm để lưu lợi nhuận theo thiết bị
    DROP TABLE IF EXISTS LoiNhuanTheoThietBi;
    CREATE TABLE LoiNhuanTheoThietBi (
        MaThietBi INT,
        TenThietBi VARCHAR(100),
        TongNhap DECIMAL(25, 2) NOT NULL DEFAULT 0,
        TongXuat DECIMAL(25, 2) NOT NULL DEFAULT 0,
        LoiNhuan DECIMAL(25, 2) NOT NULL DEFAULT 0
    );

    -- Tính tổng nhập hàng theo thiết bị
    INSERT INTO LoiNhuanTheoThietBi (MaThietBi, TenThietBi, TongNhap)
    SELECT 
        T.MaThietBi,
        T.TenThietBi,
        IFNULL(SUM(CPN.SoLuong * CPN.DonGia), 0) AS TongNhap
    FROM 
        ThietBi T
    LEFT JOIN 
        ChiTietPhieuNhap CPN ON T.MaThietBi = CPN.MaThietBi
    GROUP BY 
        T.MaThietBi;

    -- Tính tổng xuất hàng theo thiết bị
    UPDATE LoiNhuanTheoThietBi LNTB
    JOIN (
        SELECT 
            CTX.MaThietBi,
            IFNULL(SUM(CTX.SoLuong * CTX.DonGia), 0) AS TongXuat
        FROM 
            ChiTietPhieuXuat CTX
        GROUP BY 
            CTX.MaThietBi
    ) AS X ON LNTB.MaThietBi = X.MaThietBi
    SET LNTB.TongXuat = X.TongXuat;

    -- Tính lợi nhuận
    UPDATE LoiNhuanTheoThietBi
    SET LoiNhuan = TongXuat - TongNhap;

    -- Trả về kết quả lợi nhuận
    SELECT * FROM LoiNhuanTheoThietBi;
END //
DELIMITER ;

drop procedure ThongKeSanPhamDaXuat;
call ThongKeSanPhamDaXuat();



-- Bảng tồn kho các sản phẩm --
DELIMITER $$
CREATE PROCEDURE GetTonKho()
BEGIN
    SELECT 
        th.MaThietBi,
        th.TenThietBi,
        COALESCE(SUM(ctnp.SoLuong), 0) AS SoLuongNhap,
        COALESCE(SUM(ctpx.SoLuong), 0) AS SoLuongXuat,
        COALESCE(SUM(ctnp.SoLuong), 0) - COALESCE(SUM(ctpx.SoLuong), 0) AS SoLuongTon
    FROM 
        ThietBi th
    LEFT JOIN 
        ChiTietPhieuNhap ctnp ON th.MaThietBi = ctnp.MaThietBi
    LEFT JOIN 
        ChiTietPhieuXuat ctpx ON th.MaThietBi = ctpx.MaThietBi
    GROUP BY 
        th.MaThietBi, th.TenThietBi;
END$$
DELIMITER ;
call GetTonKho();

-- số khách hàng mỗi tỉnh --
CREATE VIEW SoLuongKhachHangTheoTinh AS
SELECT 
    TRIM(SUBSTRING_INDEX(DiaChi, ',', -1)) AS Tinh,  -- Lấy phần sau dấu phẩy cuối cùng và loại bỏ khoảng trắng
    COUNT(*) AS SoLuongKhachHang
FROM 
    KhachHang
WHERE 
    DiaChi IS NOT NULL  -- Đảm bảo không tính các khách hàng không có địa chỉ
GROUP BY 
    Tinh
ORDER BY 
    SoLuongKhachHang DESC  -- Sắp xếp theo số lượng khách hàng giảm dần
LIMIT 10;  -- Giới hạn lấy ra 10 tỉnh có số lượng khách hàng cao nhất

drop view SoLuongKhachHangTheoTinh;
SELECT * FROM SoLuongKhachHangTheoTinh;


-- số lượng máy bán theo mỗi tỉnh --
CREATE VIEW SoLuongMayBanTheoTinh AS
SELECT 
    TRIM(SUBSTRING_INDEX(KhachHang.DiaChi, ',', -1)) AS Tinh,  -- Extract province from address
    SUM(ChiTietPhieuXuat.SoLuong) AS SoLuongMayBan
FROM 
    ChiTietPhieuXuat
JOIN 
    PhieuXuat ON ChiTietPhieuXuat.MaPhieuXuat = PhieuXuat.MaPhieuXuat
JOIN 
    KhachHang ON PhieuXuat.MaKhachHang = KhachHang.MaKhachHang
WHERE 
    KhachHang.DiaChi IS NOT NULL  -- Ensure customers have addresses
GROUP BY 
    Tinh
ORDER BY 
    SoLuongMayBan DESC
LIMIT 10;  -- Get the top 10 provinces with the highest sales
drop view SoLuongMayBanTheoTinh;
SELECT * FROM SoLuongMayBanTheoTinh;

-- xuất hóa đơn --
DELIMITER //

CREATE PROCEDURE XuatHoaDonTheoSoDienThoaiVaNgay(
    IN phone_number VARCHAR(15),
    IN ngay_xuat DATE
)
BEGIN
    DECLARE clean_phone_number VARCHAR(15);
    -- Error handler to rollback on SQL exception
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'An error occurred, transaction rolled back' AS Message;
    END;
    -- Start Transaction
    START TRANSACTION;
    -- Clean up the phone number input
    SET clean_phone_number = TRIM(phone_number);
    -- Select statement to retrieve invoice details
    SELECT 
        KhachHang.TenKhachHang AS HoTenKhachHang,
        KhachHang.SoDienThoai AS SoDienThoaiKhachHang,
        NhanVien.HoTen AS HoTenNhanVien,
        PhieuXuat.MaPhieuXuat,
        ThietBi.MaThietBi,
        ThietBi.TenThietBi,
        ChiTietPhieuXuat.SoLuong,
        ChiTietPhieuXuat.DonGia,
        (ChiTietPhieuXuat.SoLuong * ChiTietPhieuXuat.DonGia) AS ThanhTien
    FROM 
        PhieuXuat
    JOIN 
        ChiTietPhieuXuat ON PhieuXuat.MaPhieuXuat = ChiTietPhieuXuat.MaPhieuXuat
    JOIN 
        KhachHang ON PhieuXuat.MaKhachHang = KhachHang.MaKhachHang
    JOIN 
        NhanVien ON PhieuXuat.MaNhanVien = NhanVien.MaNhanVien
    JOIN 
        ThietBi ON ChiTietPhieuXuat.MaThietBi = ThietBi.MaThietBi
    WHERE 
        KhachHang.SoDienThoai = clean_phone_number
        AND PhieuXuat.NgayXuat = ngay_xuat;
    -- Conditional rollback or commit based on query result
    IF ROW_COUNT() = 0 THEN
        ROLLBACK;
        SELECT 'Không tồn tại số điện thoại hoặc ngày lập phiếu xuất' AS Message;
    ELSE
        COMMIT;
    END IF;
END //
DELIMITER ;
CALL XuatHoaDonTheoSoDienThoaiVaNgay("0943210987", '2024-01-19');
drop procedure XuatHoaDonTheoSoDienThoaiVaNgay;
select * from phieuxuat as px
join khachhang as kh on px.MaKhachHang = kh.MaKhachHang ;

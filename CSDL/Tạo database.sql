CREATE DATABASE ComputerStore;

USE ComputerStore;
-- User --
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 2. Bảng Nhà sản xuất
CREATE TABLE NhaSanXuat (
    MaNSX INT AUTO_INCREMENT PRIMARY KEY,
    TenNSX VARCHAR(100) NOT NULL,
    QuocGia VARCHAR(50)
);
-- 3. Bảng Nhà cung cấp
CREATE TABLE NhaCungCap (
    MaNhaCungCap INT AUTO_INCREMENT PRIMARY KEY,
    TenNhaCungCap VARCHAR(100) NOT NULL,
    DiaChi VARCHAR(255),
    SoDienThoai VARCHAR(15)
);

-- 4. Bảng Loại Thiết bị
CREATE TABLE LoaiThietBi (
    MaLoai INT AUTO_INCREMENT PRIMARY KEY,
    TenLoai VARCHAR(100) NOT NULL,
    DonViTinh VARCHAR(50),
    GhiChu VARCHAR(255)
);

-- 5. Bảng Khách hàng
CREATE TABLE KhachHang (
    MaKhachHang INT AUTO_INCREMENT PRIMARY KEY,
    TenKhachHang VARCHAR(100) NOT NULL,
    DiaChi VARCHAR(255),
    SoDienThoai VARCHAR(15)
);

-- 6. Bảng Nhân viên
CREATE TABLE NhanVien (
    MaNhanVien INT AUTO_INCREMENT PRIMARY KEY,
    HoTen VARCHAR(100) NOT NULL,
    NgaySinh DATE,
    SoDienThoai VARCHAR(15)
);

-- 7. Bảng Phiếu nhập
CREATE TABLE PhieuNhap (
    MaPhieuNhap INT AUTO_INCREMENT PRIMARY KEY,
    MaNhanVien INT,
    MaNhaCungCap INT,
    NgayNhap DATE,
    FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    FOREIGN KEY (MaNhaCungCap) REFERENCES NhaCungCap(MaNhaCungCap)
);

-- 1. Bảng Thiết bị
CREATE TABLE ThietBi (
    MaThietBi INT AUTO_INCREMENT PRIMARY KEY,
    TenThietBi VARCHAR(100) NOT NULL,
    MaNSX INT,
    ThongSoKT VARCHAR(255),
    MaLoai INT,
    GiaThanh DECIMAL(10, 2) NOT NULL,
    MaNhaCungCap INT,
    FOREIGN KEY (MaNSX) REFERENCES NhaSanXuat(MaNSX),
    FOREIGN KEY (MaLoai) REFERENCES LoaiThietBi(MaLoai),
    FOREIGN KEY (MaNhaCungCap) REFERENCES NhaCungCap(MaNhaCungCap)
);


-- 8. Bảng Chi tiết Phiếu nhập
CREATE TABLE ChiTietPhieuNhap (
    MaPhieuNhap INT,
    MaThietBi INT,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (MaPhieuNhap, MaThietBi),
    FOREIGN KEY (MaPhieuNhap) REFERENCES PhieuNhap(MaPhieuNhap),
    FOREIGN KEY (MaThietBi) REFERENCES ThietBi(MaThietBi)
);

-- 9. Bảng Phiếu xuất
CREATE TABLE PhieuXuat (
    MaPhieuXuat INT AUTO_INCREMENT PRIMARY KEY,
    MaNhanVien INT,
    MaKhachHang INT,
    NgayXuat DATE,
    FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang)
);
-- 10. Bảng Chi tiết Phiếu xuất
CREATE TABLE ChiTietPhieuXuat (
    MaPhieuXuat INT,
    MaThietBi INT,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (MaPhieuXuat, MaThietBi),
    FOREIGN KEY (MaPhieuXuat) REFERENCES PhieuXuat(MaPhieuXuat),
    FOREIGN KEY (MaThietBi) REFERENCES ThietBi(MaThietBi)
);

ALTER TABLE ThietBi ADD COLUMN SoLuong INT DEFAULT 0;

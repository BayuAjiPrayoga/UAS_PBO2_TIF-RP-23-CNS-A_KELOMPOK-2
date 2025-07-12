@echo off
echo ======================================
echo    KASIRIN Database Setup
echo ======================================
echo.
echo Pastikan MySQL server sudah berjalan!
echo.
echo Login credentials untuk testing:
echo Admin: username=admin, password=admin123
echo Kasir: username=kasir01, password=kasir123
echo.
pause
echo.
echo Membuat database dan tabel...
mysql -u root -p < database_setup.sql
echo.
echo Setup database selesai!
echo.
pause

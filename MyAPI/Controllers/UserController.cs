using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MyAPI.Database;
using MyAPI.DTOs;
using MyAPI.Model;
using System.Security.Claims;

namespace MyAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class UserController : ControllerBase
    {
        private readonly MyDBContext _context;
        private readonly IWebHostEnvironment _environment;

        private readonly PasswordHasher<User> _passwordHasher;

        public UserController(
            MyDBContext context,
            IWebHostEnvironment environment)
        {
            _context = context;
            _environment = environment;

            _passwordHasher = new PasswordHasher<User>();
        }

        // GET: api/User/me
        [HttpGet("me")]
        public async Task<IActionResult> GetCurrentUser()
        {
            var userIdClaim = User.FindFirst(
                ClaimTypes.NameIdentifier
            )?.Value;

            if (!int.TryParse(userIdClaim, out int userId))
            {
                return Unauthorized();
            }

            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
            {
                return NotFound("Không tìm thấy tài khoản.");
            }

            return Ok(new
            {
                id = user.Id,
                email = user.Email,
                username = user.Username,
                avatar = user.Avatar,
                role = user.Role
            });
        }


        // PUT: api/User/profile
        [HttpPut("profile")]
        public async Task<IActionResult> UpdateProfile(
            SetupProfileRequest request)
        {
            var userIdClaim = User.FindFirst(
                ClaimTypes.NameIdentifier
            )?.Value;

            if (!int.TryParse(userIdClaim, out int userId))
            {
                return Unauthorized();
            }

            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
            {
                return NotFound("Không tìm thấy tài khoản.");
            }

            if (string.IsNullOrWhiteSpace(request.Username))
            {
                return BadRequest(
                    "Tên hiển thị không được để trống."
                );
            }

            string? oldAvatar = user.Avatar;

            user.Username = request.Username;
            user.Avatar = request.Avatar;

            await _context.SaveChangesAsync();

            // Chỉ xóa khi avatar thực sự thay đổi
            if (oldAvatar != user.Avatar)
            {
                var webRoot = _environment.WebRootPath
                    ?? Path.Combine(
                        _environment.ContentRootPath,
                        "wwwroot"
                    );

                DeleteOldAvatar(oldAvatar, webRoot);
            }

            return Ok(new
            {
                message = "Cập nhật hồ sơ thành công.",
                username = user.Username,
                avatar = user.Avatar
            });
        }

        // POST: api/User/avatar
        [HttpPost("avatar")]
        [Consumes("multipart/form-data")]
        public async Task<IActionResult> UploadAvatar(IFormFile image)
        {
            // 1. Lấy UserId từ JWT
            var userIdClaim = User.FindFirst(
                ClaimTypes.NameIdentifier
            )?.Value;

            if (!int.TryParse(userIdClaim, out int userId))
            {
                return Unauthorized();
            }

            // 2. Kiểm tra ảnh
            if (image == null || image.Length == 0)
            {
                return BadRequest("Vui lòng chọn ảnh.");
            }

            // Giới hạn 5 MB
            if (image.Length > 5 * 1024 * 1024)
            {
                return BadRequest("Ảnh không được vượt quá 5 MB.");
            }

            // 3. Kiểm tra định dạng
            var allowedExtensions = new[]
            {
        ".jpg",
        ".jpeg",
        ".png",
        ".webp"
    };

            var extension = Path.GetExtension(
                image.FileName
            ).ToLowerInvariant();

            if (!allowedExtensions.Contains(extension))
            {
                return BadRequest("Định dạng ảnh không hợp lệ.");
            }

            // 4. Tìm người dùng
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
            {
                return NotFound("Không tìm thấy tài khoản.");
            }

            // 5. Xác định thư mục lưu ảnh
            var webRoot = _environment.WebRootPath
                ?? Path.Combine(
                    _environment.ContentRootPath,
                    "wwwroot"
                );

            var folderPath = Path.Combine(
                webRoot,
                "uploads",
                "avatars"
            );

            Directory.CreateDirectory(folderPath);

            // 6. Tạo tên file ngẫu nhiên
            var fileName = $"{Guid.NewGuid()}{extension}";

            var filePath = Path.Combine(
                folderPath,
                fileName
            );


            // 7. Lấy đường dẫn avatar cũ
            string? oldAvatar = user.Avatar;

            // 8. Lưu ảnh mới và cập nhật database
            try
            {
                await using var stream = new FileStream(
                    filePath,
                    FileMode.CreateNew
                );

                await image.CopyToAsync(stream);

                // Cập nhật đường dẫn ảnh mới
                user.Avatar = $"/uploads/avatars/{fileName}";

                await _context.SaveChangesAsync();
            }
            catch
            {
                // Nếu lưu ảnh hoặc cập nhật database thất bại,
                // xóa file ảnh mới đã tạo (nếu có).
                if (System.IO.File.Exists(filePath))
                {
                    System.IO.File.Delete(filePath);
                }

                throw;
            }

            // 9. Xóa ảnh upload cũ sau khi database đã cập nhật thành công
            try
            {
                DeleteOldAvatar(oldAvatar, webRoot);
            }
            catch (IOException ex)
            {
                // Không xóa ảnh mới nếu việc xóa ảnh cũ thất bại.
                // Ghi log nếu cần.
            }

            // 10. Trả kết quả
            return Ok(new
            {
                message = "Cập nhật ảnh đại diện thành công.",
                avatar = user.Avatar
            });
        }

        private void DeleteOldAvatar(
    string? oldAvatar,
    string webRoot)
        {
            if (string.IsNullOrWhiteSpace(oldAvatar))
            {
                return;
            }

            // Chỉ xóa ảnh do người dùng upload
            const string prefix = "/uploads/avatars/";

            if (!oldAvatar.StartsWith(
                    prefix,
                    StringComparison.OrdinalIgnoreCase))
            {
                return;
            }

            // Lấy tên file, không sử dụng đường dẫn tùy ý từ database
            var fileName = oldAvatar.Substring(prefix.Length);

            if (string.IsNullOrWhiteSpace(fileName) ||
                fileName != Path.GetFileName(fileName))
            {
                return;
            }

            var filePath = Path.Combine(
                webRoot,
                "uploads",
                "avatars",
                fileName
            );

            if (System.IO.File.Exists(filePath))
            {
                System.IO.File.Delete(filePath);
            }
        }

        [HttpPut("change-password")]
        public async Task<IActionResult> ChangePassword(
    ChangePasswordRequest request)
        {
            // 1. Lấy UserId từ JWT
            var userIdClaim = User.FindFirst(
                ClaimTypes.NameIdentifier
            )?.Value;

            if (!int.TryParse(userIdClaim, out int userId))
            {
                return Unauthorized();
            }


            // 2. Tìm user trong database
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
            {
                return NotFound("Không tìm thấy tài khoản.");
            }


            // 3. Kiểm tra mật khẩu hiện tại
            var result = _passwordHasher.VerifyHashedPassword(
                user,
                user.PasswordHash,
                request.CurrentPassword
            );

            if (result == PasswordVerificationResult.Failed)
            {
                return BadRequest("Mật khẩu hiện tại không đúng.");
            }


            // 4. Kiểm tra mật khẩu mới
            if (string.IsNullOrWhiteSpace(request.NewPassword))
            {
                return BadRequest(
                    "Mật khẩu mới không được để trống."
                );
            }

            if (request.NewPassword.Length < 6)
            {
                return BadRequest(
                    "Mật khẩu mới phải có ít nhất 6 ký tự."
                );
            }


            // 5. Không cho mật khẩu mới giống mật khẩu cũ
            var samePassword =
                _passwordHasher.VerifyHashedPassword(
                    user,
                    user.PasswordHash,
                    request.NewPassword
                );

            if (samePassword != PasswordVerificationResult.Failed)
            {
                return BadRequest(
                    "Mật khẩu mới phải khác mật khẩu hiện tại."
                );
            }


            // 6. Hash mật khẩu mới
            user.PasswordHash =
                _passwordHasher.HashPassword(
                    user,
                    request.NewPassword
                );


            // 7. Lưu database
            await _context.SaveChangesAsync();


            return Ok(new
            {
                message = "Đổi mật khẩu thành công."
            });
        }
    }

}
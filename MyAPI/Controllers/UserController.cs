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

        private readonly PasswordHasher<User>
            _passwordHasher;

        public UserController(MyDBContext context)
        {
            _context = context;

            _passwordHasher =
                new PasswordHasher<User>();
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

            user.Username = request.Username;
            user.Avatar = request.Avatar;

            await _context.SaveChangesAsync();

            return Ok(new
            {
                message = "Cập nhật hồ sơ thành công.",
                username = user.Username,
                avatar = user.Avatar
            });
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
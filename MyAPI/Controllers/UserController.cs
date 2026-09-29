using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MyAPI.Database;
using MyAPI.DTOs;
using System.Security.Claims;

namespace MyAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class UserController : ControllerBase
    {
        private readonly MyDBContext _context;

        public UserController(MyDBContext context)
        {
            _context = context;
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
    }
}
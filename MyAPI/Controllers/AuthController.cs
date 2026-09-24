using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MyAPI.Database;
using MyAPI.DTOs;
using MyAPI.Model;
using MyAPI.Model;
using MailKit.Net.Smtp;
using MimeKit;

namespace MyAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly MyDBContext _context;
        private readonly PasswordHasher<User> _passwordHasher;
        private readonly IConfiguration _configuration;

        public AuthController(
            MyDBContext context,
            IConfiguration configuration)
        {
            _context = context;
            _configuration = configuration;
            _passwordHasher = new PasswordHasher<User>();
        }

        // REGISTER
        [HttpPost("register")]
        public async Task<IActionResult> Register(RegisterRequest request)
        {
            // Kiểm tra email
            var emailExists = await _context.Users
                .AnyAsync(u => u.Email == request.Email);

            if (emailExists)
            {
                return BadRequest("Email đã tồn tại.");
            }

            // Tạo User
            var user = new User
            {
                Email = request.Email
            };

            // Hash password
            user.PasswordHash = _passwordHasher.HashPassword(
                user,
                request.Password
            );

            // Lưu database
            _context.Users.Add(user);

            await _context.SaveChangesAsync();

            return Ok(new
            {
                message = "Đăng ký thành công.",
                userId = user.Id,
                email = user.Email
            });
        }

        // LOGIN
        [HttpPost("login")]
        public async Task<IActionResult> Login(LoginRequest request)
        {
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Email == request.Email);

            if (user == null)
            {
                return Unauthorized("Email hoặc password không đúng.");
            }

            var result = _passwordHasher.VerifyHashedPassword(
                user,
                user.PasswordHash,
                request.Password
            );

            if (result == PasswordVerificationResult.Failed)
            {
                return Unauthorized("Email hoặc password không đúng.");
            }

            return Ok(new
            {
                message = "Đăng nhập thành công.",
                userId = user.Id,
                username = user.Username,
                email = user.Email
            });
        }
        // FORGOT PASSWORD
        [HttpPost("forgot-password")]
        public async Task<IActionResult> ForgotPassword(
    ForgotPasswordRequest request)
        {
            // Kiểm tra email có tồn tại không
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Email == request.Email);

            if (user == null)
            {
                return NotFound("Email chưa được đăng ký.");
            }

            // Tạo OTP 6 số
            Random random = new Random();
            string otp = random.Next(100000, 1000000).ToString();

            // OTP hết hạn sau 5 phút
            var expiryTime = DateTime.Now.AddMinutes(5);

            // Lưu OTP vào database
            var resetToken = new PasswordResetToken
            {
                Email = request.Email,
                OTP = otp,
                ExpiryTime = expiryTime,
                IsUsed = false
            };

            _context.PasswordResetTokens.Add(resetToken);

            await _context.SaveChangesAsync();

            // Lấy thông tin Gmail từ appsettings.json
            var emailSettings = _configuration
                .GetSection("EmailSettings")
                .Get<EmailSettings>();

            // Tạo email
            var message = new MimeMessage();

            message.From.Add(
                new MailboxAddress(
                    "MyAPI",
                    emailSettings.Email
                )
            );

            message.To.Add(
                new MailboxAddress(
                    user.Username,
                    user.Email
                )
            );

            message.Subject = "Mã OTP đặt lại mật khẩu";

            message.Body = new TextPart("plain")
            {
                Text =
                    $"Xin chào {user.Username},\n\n" +
                    $"Mã OTP của bạn là: {otp}\n\n" +
                    $"Mã có hiệu lực trong 5 phút.\n\n" +
                    $"Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này."
            };

            // Gửi email
            using (var smtp = new SmtpClient())
            {
                await smtp.ConnectAsync(
                    emailSettings.SmtpServer,
                    emailSettings.Port,
                    MailKit.Security.SecureSocketOptions.StartTls
                );

                await smtp.AuthenticateAsync(
                    emailSettings.Email,
                    emailSettings.Password
                );

                await smtp.SendAsync(message);

                await smtp.DisconnectAsync(true);
            }

            return Ok(new
            {
                message = "OTP đã được gửi đến email."
            });
        }
        // VERIFY OTP

            [HttpPost("verify-otp")]
            public async Task<IActionResult> VerifyOtp(
        VerifyOtpRequest request)
            {
                // Tìm OTP trong database
                var resetToken = await _context.PasswordResetTokens
                    .Where(x =>
                        x.Email == request.Email &&
                        x.OTP == request.OTP &&
                        !x.IsUsed)
                    .OrderByDescending(x => x.Id)
                    .FirstOrDefaultAsync();

                // Không tìm thấy OTP
                if (resetToken == null)
                {
                    return BadRequest("OTP không đúng.");
                }

                // Kiểm tra OTP hết hạn
                if (resetToken.ExpiryTime < DateTime.Now)
                {
                    return BadRequest("OTP đã hết hạn.");
                }

                return Ok(new
                {
                    message = "OTP chính xác."
                });
        }

        // RESET PASSWORD
      
            [HttpPost("reset-password")]
            public async Task<IActionResult> ResetPassword(
        ResetPasswordRequest request)
            {
                // Tìm OTP
                var resetToken = await _context.PasswordResetTokens
                    .Where(x =>
                        x.Email == request.Email &&
                        x.OTP == request.OTP &&
                        !x.IsUsed)
                    .OrderByDescending(x => x.Id)
                    .FirstOrDefaultAsync();

                // Kiểm tra OTP
                if (resetToken == null)
                {
                    return BadRequest("OTP không đúng.");
                }

                // Kiểm tra OTP hết hạn
                if (resetToken.ExpiryTime < DateTime.Now)
                {
                    return BadRequest("OTP đã hết hạn.");
                }

                // Tìm user
                var user = await _context.Users
                    .FirstOrDefaultAsync(u => u.Email == request.Email);

                if (user == null)
                {
                    return NotFound("Không tìm thấy tài khoản.");
                }

                // Hash password mới
                user.PasswordHash = _passwordHasher.HashPassword(
                    user,
                    request.NewPassword
                );

                // Đánh dấu OTP đã sử dụng
                resetToken.IsUsed = true;

                // Lưu database
                await _context.SaveChangesAsync();

                return Ok(new
                {
                    message = "Đổi mật khẩu thành công."
                });
            }

        }
    }
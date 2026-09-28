using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MyAPI.Database;
using MyAPI.DTOs;
using MyAPI.Model;
using MailKit.Net.Smtp;
using MimeKit;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;

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

        private string GenerateJwtToken(User user)
        {
            var jwtSettings =
                _configuration.GetSection("JwtSettings");

            var keyString = jwtSettings["Key"]
                ?? throw new InvalidOperationException(
                    "JWT Key chưa được cấu hình."
                );

            var key = new SymmetricSecurityKey(
                Encoding.UTF8.GetBytes(keyString)
            );

            var credentials =
                new SigningCredentials(
                    key,
                    SecurityAlgorithms.HmacSha256
                );

            var claims = new List<Claim>
    {
        new Claim(
            ClaimTypes.NameIdentifier,
            user.Id.ToString()
        ),

        new Claim(
            ClaimTypes.Email,
            user.Email
        )
    };

            var token = new JwtSecurityToken(
                issuer: jwtSettings["Issuer"],
                audience: jwtSettings["Audience"],
                claims: claims,
                expires: DateTime.UtcNow.AddMinutes(
                    int.Parse(
                        jwtSettings["ExpireMinutes"] ?? "60"
                    )
                ),
                signingCredentials: credentials
            );

            return new JwtSecurityTokenHandler()
                .WriteToken(token);
        }

        // REGISTER
        [HttpPost("register")]
        public async Task<IActionResult> Register(RegisterRequest request)
        {
            // Kiểm tra email đã có tài khoản chính thức chưa
            var emailExists = await _context.Users
                .AnyAsync(u => u.Email == request.Email);

            if (emailExists)
            {
                return BadRequest("Email đã tồn tại.");
            }


            // Nếu trước đó email này đã đăng ký nhưng chưa nhập OTP
            // thì xóa đăng ký tạm cũ
            var oldPending = await _context.PendingRegistrations
                .FirstOrDefaultAsync(p => p.Email == request.Email);

            if (oldPending != null)
            {
                _context.PendingRegistrations.Remove(oldPending);
            }


            // Tạo object User tạm để dùng PasswordHasher<User>
            var tempUser = new User
            {
                Email = request.Email
            };

            // Hash password
            string passwordHash =
                _passwordHasher.HashPassword(
                    tempUser,
                    request.Password
                );


            // Tạo OTP 6 số
            Random random = new Random();

            string otp =
                random.Next(100000, 1000000)
                    .ToString();


            // OTP có hiệu lực 5 phút
            var expiryTime =
                DateTime.Now.AddMinutes(5);


            // Lưu đăng ký tạm
            var pendingRegistration =
                new PendingRegistration
                {
                    Email = request.Email,
                    PasswordHash = passwordHash,
                    OTP = otp,
                    ExpiryTime = expiryTime
                };


            _context.PendingRegistrations
                .Add(pendingRegistration);

            await _context.SaveChangesAsync();


            // Lấy cấu hình Gmail
            var emailSettings =
                _configuration
                    .GetSection("EmailSettings")
                    .Get<EmailSettings>();


            // Tạo email
            var message = new MimeMessage();

            message.From.Add(
                new MailboxAddress(
                    "SmartFood",
                    emailSettings.Email
                )
            );

            message.To.Add(
                new MailboxAddress(
                    request.Email,
                    request.Email
                )
            );

            message.Subject =
                "Mã OTP xác nhận đăng ký SmartFood";

            message.Body =
                new TextPart("plain")
                {
                    Text =
                        $"Xin chào,\n\n" +
                        $"Mã OTP đăng ký SmartFood của bạn là: {otp}\n\n" +
                        $"Mã có hiệu lực trong 5 phút.\n\n" +
                        $"Nếu bạn không thực hiện đăng ký, hãy bỏ qua email này."
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

        // UPDATE PROFILE
        [Authorize]
        [HttpPut("profile")]
        public async Task<IActionResult> UpdateProfile(
    SetupProfileRequest request)
        {
            // Lấy UserId từ JWT
            var userIdClaim = User.FindFirst(
                ClaimTypes.NameIdentifier
            );

            if (userIdClaim == null)
            {
                return Unauthorized("Token không hợp lệ.");
            }

            int userId = int.Parse(userIdClaim.Value);

            // Tìm User
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Id == userId);

            if (user == null)
            {
                return NotFound("Không tìm thấy tài khoản.");
            }

            // Kiểm tra username
            if (string.IsNullOrWhiteSpace(request.Username))
            {
                return BadRequest(
                    "Tên hiển thị không được để trống."
                );
            }

            // Cập nhật
            user.Username = request.Username;
            user.Avatar = request.Avatar;

            await _context.SaveChangesAsync();

            return Ok(new
            {
                message = "Thiết lập hồ sơ thành công.",
                username = user.Username,
                avatar = user.Avatar
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

            string token = GenerateJwtToken(user);

            return Ok(new
            {
                message = "Đăng nhập thành công.",
                token = token,
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
            // XÁC THỰC OTP ĐĂNG KÝ
            if (request.Purpose == "REGISTER")
            {
                var pending = await _context.PendingRegistrations
                    .FirstOrDefaultAsync(x =>
                        x.Email == request.Email &&
                        x.OTP == request.OTP
                    );

                // Không tìm thấy
                if (pending == null)
                {
                    return BadRequest("OTP không đúng.");
                }

                // OTP hết hạn
                if (pending.ExpiryTime < DateTime.Now)
                {
                    return BadRequest("OTP đã hết hạn.");
                }

                // Kiểm tra lại email trước khi tạo User
                var emailExists = await _context.Users
                    .AnyAsync(u => u.Email == request.Email);

                if (emailExists)
                {
                    return BadRequest("Email đã được đăng ký.");
                }

                // OTP chính xác -> tạo User thật
                var user = new User
                {
                    Email = pending.Email,
                    PasswordHash = pending.PasswordHash
                };

                _context.Users.Add(user);

                // Xóa đăng ký tạm
                _context.PendingRegistrations.Remove(pending);

                await _context.SaveChangesAsync();

                string token = GenerateJwtToken(user);

                return Ok(new
                {
                    message = "Xác nhận đăng ký thành công.",
                    token = token
                });
            }
            // XÁC THỰC OTP QUÊN MẬT KHẨU
            if (request.Purpose == "RESET_PASSWORD")
            {
                var resetToken = await _context.PasswordResetTokens
                    .Where(x =>
                        x.Email == request.Email &&
                        x.OTP == request.OTP &&
                        !x.IsUsed)
                    .OrderByDescending(x => x.Id)
                    .FirstOrDefaultAsync();

                if (resetToken == null)
                {
                    return BadRequest("OTP không đúng.");
                }

                if (resetToken.ExpiryTime < DateTime.Now)
                {
                    return BadRequest("OTP đã hết hạn.");
                }

                return Ok(new
                {
                    message = "OTP chính xác."
                });
            }

            // Purpose không hợp lệ
            return BadRequest("Mục đích xác thực OTP không hợp lệ.");
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
using Microsoft.AspNetCore.Authorization;
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
    public class FoodItemsController : ControllerBase
    {
        private readonly MyDBContext _context;
        private readonly IWebHostEnvironment _environment;

        public FoodItemsController(
            MyDBContext context,
            IWebHostEnvironment environment)
        {
            _context = context;
            _environment = environment;
        }


        [HttpPost("manual")]
        public async Task<IActionResult> AddManualFood(
            [FromForm] AddManualFoodRequest request)
        {
            // 1. Lấy UserId từ JWT
            var userIdClaim = User.FindFirst(
                ClaimTypes.NameIdentifier
            )?.Value;

            if (!int.TryParse(userIdClaim, out int userId))
            {
                return Unauthorized();
            }


            // 2. Kiểm tra Category
            var categoryExists = await _context.Categories
                .AnyAsync(c => c.Id == request.CategoryId);

            if (!categoryExists)
            {
                return BadRequest("Danh mục không hợp lệ.");
            }


            // 3. Kiểm tra số lượng
            if (request.Quantity <= 0)
            {
                return BadRequest(
                    "Số lượng phải lớn hơn 0."
                );
            }


            // 4. Xử lý ảnh
            string? imageUrl = null;

            if (request.Image != null &&
                request.Image.Length > 0)
            {
                imageUrl = await SaveImage(request.Image);
            }


            // 5. Tạo FoodItem
            var foodItem = new FoodItem
            {
                UserId = userId,

                // Nhập thủ công nên không liên kết Foods
                FoodId = null,

                CustomName = request.Name.Trim(),

                CustomCategoryId =
                    request.CategoryId,

                CustomImageUrl = imageUrl,

                Quantity = request.Quantity,

                Unit = request.Unit.Trim(),

                ExpiryDate = request.ExpiryDate,

                Note = string.IsNullOrWhiteSpace(
                    request.Note)
                    ? null
                    : request.Note.Trim(),

                AddedAt = DateTime.Now
            };


            // 6. Lưu database
            _context.FoodItems.Add(foodItem);

            await _context.SaveChangesAsync();


            // 7. Trả kết quả
            return Ok(new
            {
                message =
                    "Thêm thực phẩm thành công.",

                id = foodItem.Id,

                name = foodItem.CustomName,

                imageUrl =
                    foodItem.CustomImageUrl
            });
        }


        private async Task<string> SaveImage(
            IFormFile image)
        {
            // Kiểm tra định dạng ảnh
            var allowedExtensions =
                new[] { ".jpg", ".jpeg", ".png", ".webp" };

            var extension =
                Path.GetExtension(
                    image.FileName
                ).ToLowerInvariant();

            if (!allowedExtensions.Contains(extension))
            {
                throw new InvalidOperationException(
                    "Định dạng ảnh không hợp lệ."
                );
            }


            // Giới hạn 5 MB
            if (image.Length > 5 * 1024 * 1024)
            {
                throw new InvalidOperationException(
                    "Ảnh không được vượt quá 5 MB."
                );
            }


            // wwwroot/uploads/food-items
            var folderPath = Path.Combine(
                _environment.WebRootPath,
                "uploads",
                "food-items"
            );

            Directory.CreateDirectory(folderPath);


            // Tạo tên file ngẫu nhiên
            var fileName =
                $"{Guid.NewGuid()}{extension}";

            var filePath = Path.Combine(
                folderPath,
                fileName
            );


            await using var stream =
                new FileStream(
                    filePath,
                    FileMode.Create
                );

            await image.CopyToAsync(stream);


            return $"/uploads/food-items/{fileName}";
        }
    }
}
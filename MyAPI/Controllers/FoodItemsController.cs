using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MyAPI.Database;
using MyAPI.DTOs;
using MyAPI.Model;
using System.Security.Claims;
using System.Net;
using System.Text.Json;
using System.Data;

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

        [HttpPost("barcode")]
        public async Task<IActionResult> AddBarcodeFood(
    [FromForm] AddBarcodeFoodRequest request,
    [FromServices] IHttpClientFactory httpClientFactory)
        {
            var userIdClaim = User.FindFirst(
                ClaimTypes.NameIdentifier)?.Value;

            if (!int.TryParse(userIdClaim, out int userId))
                return Unauthorized();

            var barcode = request.Barcode.Trim();
            var name = request.Name.Trim();
            var unit = request.Unit.Trim();

            if (string.IsNullOrWhiteSpace(name))
                return BadRequest("Tên thực phẩm không được để trống.");

            if (string.IsNullOrWhiteSpace(unit))
                return BadRequest("Đơn vị không được để trống.");

            if (request.Quantity <= 0)
                return BadRequest("Số lượng phải lớn hơn 0.");

            var categoryExists = await _context.Categories
                .AnyAsync(c => c.Id == request.CategoryId);

            if (!categoryExists)
                return BadRequest("Danh mục không hợp lệ.");

            // Kiểm tra ảnh người dùng tải lên
            if (request.Image != null)
            {
                var extension = Path.GetExtension(
                    request.Image.FileName).ToLowerInvariant();

                if (!new[] { ".jpg", ".jpeg", ".png", ".webp" }
                    .Contains(extension))
                    return BadRequest("Định dạng ảnh không hợp lệ.");

                if (request.Image.Length > 5 * 1024 * 1024)
                    return BadRequest("Ảnh không được vượt quá 5 MB.");
            }

            // Tìm ảnh từ Open Food Facts khi cần tạo Food mới
            string? openFoodFactsImageUrl = null;
            bool foundOnOpenFoodFacts = false;

            var existingFood = await _context.Foods
                .AsNoTracking()
                .AnyAsync(f => f.Barcode == barcode);

            if (!existingFood)
            {
                try
                {
                    var client = httpClientFactory.CreateClient();
                    client.Timeout = TimeSpan.FromSeconds(10);

                    var url =
                        $"https://world.openfoodfacts.org/api/v3/product/{barcode}" +
                        "?fields=code,product_name,brands,image_front_url";

                    using var httpRequest = new HttpRequestMessage(
                        HttpMethod.Get, url);

                    httpRequest.Headers.UserAgent.ParseAdd(
                        "SmartFoodStudentProject/1.0 (contact: your-email@example.com)");

                    using var response = await client.SendAsync(httpRequest);

                    if (response.IsSuccessStatusCode)
                    {
                        using var stream =
                            await response.Content.ReadAsStreamAsync();

                        using var json =
                            await JsonDocument.ParseAsync(stream);

                        var root = json.RootElement;

                        if (root.TryGetProperty("product", out var product) &&
                            product.ValueKind == JsonValueKind.Object &&
                            product.TryGetProperty("product_name", out var productName) &&
                            productName.ValueKind == JsonValueKind.String &&
                            !string.IsNullOrWhiteSpace(productName.GetString()))
                        {
                            foundOnOpenFoodFacts = true;

                            if (product.TryGetProperty(
                                    "image_front_url", out var imageElement) &&
                                imageElement.ValueKind == JsonValueKind.String)
                            {
                                var candidate = imageElement.GetString();

                                if (Uri.TryCreate(candidate, UriKind.Absolute, out var imageUri) &&
                                    imageUri.Scheme == Uri.UriSchemeHttps &&
                                    imageUri.Host.Equals(
                                        "images.openfoodfacts.org",
                                        StringComparison.OrdinalIgnoreCase))
                                {
                                    openFoodFactsImageUrl = candidate;
                                }
                            }
                        }
                    }
                    else if (response.StatusCode != HttpStatusCode.NotFound)
                    {
                        return StatusCode(502,
                            "Không thể xác minh sản phẩm với Open Food Facts. Vui lòng thử lại.");
                    }
                }
                catch (Exception ex) when (
                    ex is HttpRequestException ||
                    ex is TaskCanceledException ||
                    ex is JsonException)
                {
                    return StatusCode(502,
                        "Không thể kết nối hoặc đọc dữ liệu Open Food Facts.");
                }
            }

            // Transaction để đảm bảo lưu nhất quán cả hai bảng
            await using var transaction =
                await _context.Database.BeginTransactionAsync(
                    IsolationLevel.Serializable);

            try
            {
                var food = await _context.Foods
                    .FirstOrDefaultAsync(f => f.Barcode == barcode);

                if (food == null)
                {
                    food = new Food
                    {
                        Barcode = barcode,
                        Name = name,
                        Brand = string.IsNullOrWhiteSpace(request.Brand)
                            ? null
                            : request.Brand.Trim(),
                        CategoryId = request.CategoryId,
                        ImageUrl = openFoodFactsImageUrl,
                        ImageSource = foundOnOpenFoodFacts
                            ? "OpenFoodFacts"
                            : "User",
                        CreatedAt = DateTime.Now
                    };

                    _context.Foods.Add(food);
                    await _context.SaveChangesAsync();
                }

                // Ảnh upload riêng của lần thêm vào kho
                string? customImageUrl = null;

                if (request.Image != null && request.Image.Length > 0)
                {
                    customImageUrl = await SaveImage(request.Image);
                }

                var foodItem = new FoodItem
                {
                    UserId = userId,
                    FoodId = food.Id,
                    CustomName = null,
                    CustomCategoryId = null,
                    CustomImageUrl = customImageUrl,
                    Quantity = request.Quantity,
                    Unit = unit,
                    ExpiryDate = request.ExpiryDate,
                    Note = string.IsNullOrWhiteSpace(request.Note)
                        ? null
                        : request.Note.Trim(),
                    AddedAt = DateTime.Now
                };

                _context.FoodItems.Add(foodItem);
                await _context.SaveChangesAsync();

                await transaction.CommitAsync();

                return Ok(new
                {
                    message = "Thêm thực phẩm từ Barcode thành công.",
                    foodId = food.Id,
                    foodItemId = foodItem.Id,
                    barcode = food.Barcode,
                    name = food.Name,
                    imageUrl = customImageUrl ?? food.ImageUrl,
                    source = food.ImageSource
                });
            }
            catch
            {
                await transaction.RollbackAsync();
                throw;
            }
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
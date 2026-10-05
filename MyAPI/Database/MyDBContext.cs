using Microsoft.EntityFrameworkCore;
using MyAPI.Model;

namespace MyAPI.Database
{
    public class MyDBContext : DbContext
    {
        public MyDBContext(DbContextOptions<MyDBContext> options)
            : base(options)
        {
        }

        public DbSet<User> Users { get; set; }
        public DbSet<PasswordResetToken> PasswordResetTokens { get; set; }
        public DbSet<PendingRegistration> PendingRegistrations { get; set; }

        public DbSet<Category> Categories { get; set; }
        public DbSet<Food> Foods { get; set; }
        public DbSet<FoodItem> FoodItems { get; set; }

        protected override void OnModelCreating(
    ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Barcode không được trùng
            modelBuilder.Entity<Food>()
                .HasIndex(f => f.Barcode)
                .IsUnique();

            // Category Name không được trùng
            modelBuilder.Entity<Category>()
                .HasIndex(c => c.Name)
                .IsUnique();


            // User 1 - N FoodItems
            modelBuilder.Entity<FoodItem>()
                .HasOne(fi => fi.User)
                .WithMany(u => u.FoodItems)
                .HasForeignKey(fi => fi.UserId)
                .OnDelete(DeleteBehavior.Cascade);


            // Food 1 - N FoodItems
            modelBuilder.Entity<FoodItem>()
                .HasOne(fi => fi.Food)
                .WithMany(f => f.FoodItems)
                .HasForeignKey(fi => fi.FoodId)
                .OnDelete(DeleteBehavior.Restrict);


            // Category 1 - N Foods
            modelBuilder.Entity<Food>()
                .HasOne(f => f.Category)
                .WithMany(c => c.Foods)
                .HasForeignKey(f => f.CategoryId)
                .OnDelete(DeleteBehavior.Restrict);


            // Category 1 - N manual FoodItems
            modelBuilder.Entity<FoodItem>()
                .HasOne(fi => fi.CustomCategory)
                .WithMany(c => c.CustomFoodItems)
                .HasForeignKey(fi => fi.CustomCategoryId)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<Category>().HasData(
    new Category { Id = 1, Name = "Rau củ" },
    new Category { Id = 2, Name = "Trái cây" },
    new Category { Id = 3, Name = "Thịt" },
    new Category { Id = 4, Name = "Hải sản" },
    new Category { Id = 5, Name = "Sữa & sản phẩm từ sữa" },
    new Category { Id = 6, Name = "Đồ uống" },
    new Category { Id = 7, Name = "Đồ khô" },
    new Category { Id = 8, Name = "Gia vị" },
    new Category { Id = 9, Name = "Bánh kẹo" }
);
        }
    }
}
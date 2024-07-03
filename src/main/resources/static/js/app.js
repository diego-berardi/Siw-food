const toggleMenuBtn = document.querySelector(".toggle-menu-btn");
const mobileMenu = document.querySelector(".mobile-menu");
mobileMenu.style.display = "none";
let isDisplayNone = true;

console.log(toggleMenuBtn);
console.log(mobileMenu);

toggleMenuBtn.addEventListener("click", () => {
  console.log("pressed", isDisplayNone);
  if (isDisplayNone) {
    mobileMenu.style.display = "flex";
    isDisplayNone = false;
  } else {
    mobileMenu.style.display = "none";
    isDisplayNone = true;
  }
});


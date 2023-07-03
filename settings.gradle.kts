rootProject.name = "KirbyUtils"

include("Common", "Bukkit", "Spring", "test-plugin")
project(":Common").name = "KirbyUtils-Common"
project(":Bukkit").name = "KirbyUtils-Bukkit"
project(":Spring").name = "KirbyUtils-Spring"
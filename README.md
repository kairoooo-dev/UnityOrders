# UnityOrders

Enterprise-grade GUI-driven buy order marketplace plugin for Paper 1.21.11+ with Vault economy, enchantment support, Folia compatibility, and async-first architecture.

## Features

- **Buy Order Marketplace** &mdash; Players create buy orders for any material; others fulfill them for payment.
- **GUI-Driven** &mdash; Fully interactive inventory GUIs with pagination, no commands required.
- **Vault Economy** &mdash; Integrated with Vault for all monetary transactions.
- **PlaceholderAPI** &mdash; Exposes placeholders for order counts and statistics.
- **LuckPerms** &mdash; Detected and integrated for permission management.
- **Folia-Compatible** &mdash; Region-safe scheduling; no unsafe Bukkit API access.
- **Async-First** &mdash; Database queries and update checks run asynchronously.
- **Update Checker** &mdash; Automatic GitHub release checking with admin notifications.
- **Public API** &mdash; Other plugins can integrate via the `UnityOrdersAPI` interface.

## Requirements

- **Paper 1.21.11+** (or Folia equivalent)
- **Vault** (required, for economy)
- **PlaceholderAPI** (optional, for placeholders)
- **LuckPerms** (optional, for permissions)

## Installation

1. Download the latest release JAR from [GitHub Releases](https://github.com/kairoooo-dev/UnityOrders/releases).
2. Place the JAR in your server's `plugins/` directory.
3. Restart your server.
4. Configure `config.yml` as needed.

## Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/orders` | `unityorders.use` | Open the marketplace GUI |
| `/orders myorders` | `unityorders.use` | View your orders |
| `/orders browse` | `unityorders.use` | Browse all open orders |
| `/unityorders reload` | `unityorders.admin` | Reload configuration |
| `/unityorders stats` | `unityorders.admin` | View order statistics |
| `/unityorders cancel <id>` | `unityorders.admin` | Cancel an order |
| `/unityorders view <player>` | `unityorders.admin` | View a player's orders |

## Permissions

| Permission | Default | Description |
|-----------|---------|-------------|
| `unityorders.use` | `true` | Access to the marketplace |
| `unityorders.admin` | `op` | Administrative commands |

## PlaceholderAPI Placeholders

| Placeholder | Description |
|------------|-------------|
| `%unityorders_total%` | Total number of orders |
| `%unityorders_myorders%` | Number of orders for the player |
| `%unityorders_active%` | Number of active (unfulfilled) orders |

## API Usage

```java
// Get the API instance
UnityOrdersAPI api = (UnityOrdersAPI) Bukkit.getPluginManager().getPlugin("UnityOrders");

// Create a buy order
APIResult result = api.createOrder(player, Material.DIAMOND, 64, 10.0);
if (result.isSuccess()) {
    player.sendMessage("Order created: " + result.getMessage());
}

// Fulfill an order
APIResult fulfillResult = api.fulfillOrder(orderId, seller, 32);

// Cancel an order
APIResult cancelResult = api.cancelOrder(orderId, player);
```

## Architecture

```
com.unity.orders
├── UnityOrders.java          # Main plugin class
├── managers/                 # Business logic
│   ├── OrderManager.java     # Order lifecycle management
│   └── OrderResult.java      # Operation result wrapper
├── gui/                      # GUI system
│   ├── GuiManager.java       # GUI creation and session tracking
│   ├── GuiSession.java       # Per-player GUI session
│   └── GuiType.java          # GUI type enum
├── commands/                 # Command handlers
│   ├── OrderCommand.java     # Player-facing /orders command
│   └── AdminCommand.java     # Admin /unityorders command
├── listeners/                # Event listeners
│   ├── GuiListener.java      # Inventory click routing
│   └── PlayerListener.java   # Join/quit handling
├── hooks/                    # Soft-depend integrations
│   ├── HookManager.java      # Hook detection
│   └── papi/                 # PlaceholderAPI
│       └── PlaceholderAPIHook.java
├── api/                      # Public API
│   ├── UnityOrdersAPI.java   # API interface
│   ├── APIImplementation.java # API implementation
│   ├── APIResult.java        # API result wrapper
│   └── APINotFound.java      # API not available exception
└── utils/                    # Utilities
    └── UpdateChecker.java    # GitHub release checker
```

## Building

```bash
./gradlew build
```

The shaded JAR will be in `build/libs/UnityOrders-1.0.0.jar`.

## License

See [LICENSE](LICENSE).

## Author

**kairoooo-dev** &mdash; [GitHub](https://github.com/kairoooo-dev)

# Helidon MCP Coffee Shop Declarative

This application demonstrates a Helidon Model Context Protocol (MCP) server for a coffee shop. The server provides tools that
allow MCP clients to interact with a coffee shop's menu and order management system.

The coffee shop MCP server exposes three main tools:

1. **getMenu** - Provides the complete coffee shop menu including drinks and food items 
with their descriptions, prices, categories, tags, and available add-ons.

2. **listOrders** - Returns a list of all current orders in the system, showing customer 
names, order contents, and total prices.

3. **takeOrder** - Allows taking new orders by specifying a customer name and a list of 
menu items they want to order.

The application uses an H2 in-memory database pre-populated with a sample menu containing various coffee drinks 
(Latte, Cappuccino, Espresso, etc.), food items (Avocado Toast, Blueberry Muffin, etc.), and one sample order.
The application leverages Helidon Data, providing a unified and efficient approach to data persistence and retrieval operations.

## Build And Run The Application

```bash
mvn clean package
java -jar target/helidon-mcp-coffee-shop-declarative.jar
```

The server will start at `http://localhost:8081` with the MCP endpoint available at `/mcp-coffee-shop`.

## Exercise The Application

### Using MCP Client

The application is designed to work with MCP-compatible clients supporting MCP versions `2024-11-05` and `2025-03-26`. The test suite
demonstrates how to use it with Langchain4j MCP client:

1. **List Available Tools**: The MCP client can discover the three available tools (`getMenu`, `listOrders`, `takeOrder`)

2. **Get Menu**: Call the `getMenu` tool to retrieve the complete menu as JSON objects

3. **List Orders**: Call the `listOrders` tool to see current orders

4. **Take Order**: Call the `takeOrder` tool with an order request containing:
    - `name`: Customer name (string)
    - `content`: Array of menu item names (array of strings)

Example order request:
```json
{
  "name": "John Doe",
  "content": ["Latte", "Avocado Toast"]
}
```

### Using Claude Desktop

1. [Install Claude desktop](https://claude.ai/download)
2. Update Claude desktop configuration to register your MCP server. Edit the `claude_desktop_config.json` file located under 
`Settings -> Developer -> Edit Config` with the following content:
```json
{
  "mcpServers": {
    "helidon-coffee-shop": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "http://localhost:8081/mcp-coffee-shop"
      ]
    }
  }
}
```
3. Open claude desktop application. Claude automatically connects to the coffee shop server at startup.
4. Ask the following question:
    1. `What is on the menu today?`
    2. `Can I order a hot chocolate?`
    3. `What are the current orders?`

Note: Any application similar to Claude that support MCP can be used instead.

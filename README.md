# OpenAPI SpecTacular

IntelliJ IDEA plugin that generates realistic JSON example data from OpenAPI (3.x) and Swagger (2.x) specification files.

## Features

- **Automatic spec parsing** — Reads `.yaml`, `.yml` and `.json` OpenAPI/Swagger files and extracts all component schemas and inline request
  bodies.
- **Per-schema example count** — Choose how many examples to generate for each schema independently, or set a global count for all at once.
- **Context-aware data generation** — Produces realistic values based on property names, types and formats (e.g. emails for `email` fields,
  UUIDs for `id` fields, dates for `date`/`date-time` formats, etc.).
- **Enum support** — Cycles through defined enum values across generated examples.
- **Custom example values** — Honors `example` annotations present in the spec.
- **Nested objects and arrays** — Recursively generates data for complex schemas up to a depth of 3.
- **Individual or combined output** — Write each schema's examples to its own JSON file, or combine everything into a single file.
- **IntelliJ integration** — Launch from the Tools menu, the editor context menu, or the project view context menu on a spec file.

## Requirements

| Requirement   | Version              |
|---------------|----------------------|
| IntelliJ IDEA | 2023.3+ (build 233+) |
| Java          | 21                   |

## Installation

1. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```
2. The distributable is located at `build/distributions/openapi-generator-plugin-1.0.0.zip`.
3. In IntelliJ IDEA, go to **Settings → Plugins → ⚙️ → Install Plugin from Disk...** and select the ZIP file.
4. Restart IntelliJ IDEA.

## Usage

### From the Tools menu

**Tools → OpenAPI Generator → Generate Examples from OpenAPI Specification**

### From the context menu

Right-click a `.yaml`, `.yml` or `.json` file in the Project view or Editor and select **Generate Examples from OpenAPI Specification**.

### Dialog workflow

1. **Select spec file** — Browse to your OpenAPI specification file. If launched from the context menu, the file is pre-filled.
2. **Parse** — Click **Parse Spec**. Discovered schemas appear in the table with their property count and description.
3. **Set example counts** — Use the per-schema spinner in the *Examples* column, or set a global count with **Set all counts to** → **Apply
   to All**.
4. **Choose output directory** — Defaults to `<project>/openapi-examples`.
5. **Combine output (optional)** — Check *Combine into single JSON* to write a single `all-examples.json` file instead of one file per
   schema.
6. **Generate** — Click **Generate**. A background task writes the files, and a notification confirms the result.

### Output format

**Individual mode** — One file per schema: `<schema-name>-examples.json`

```json
[
  {
    "id": "00000400-0000-4000-8000-000000000001",
    "name": "Alice Smith",
    "email": "alice1@example.com",
    "age": 28,
    "status": "active"
  },
  {
    "id": "00000400-0000-4000-8000-000000000002",
    "name": "Bob Johnson",
    "email": "bob2@test.org",
    "age": 33,
    "status": "inactive"
  }
]
```

**Combined mode** — Single `all-examples.json`:

```json
{
  "User": [
    {
      "id": "...",
      "name": "..."
    }
  ],
  "Order": [
    {
      "orderId": "...",
      "total": 9.99
    }
  ]
}
```

## Supported data generation

The generator infers realistic values from property type, format, and name heuristics:

| Type / Format                              | Generated value                         |
|--------------------------------------------|-----------------------------------------|
| `string` + `email`                         | `alice1@example.com`                    |
| `string` + `uuid`                          | Deterministic UUID                      |
| `string` + `date`                          | ISO 8601 date                           |
| `string` + `date-time`                     | ISO 8601 datetime                       |
| `string` + `uri`/`url`                     | `https://api.example.com/resource/1`    |
| `string` + `ipv4`                          | `192.168.x.x`                           |
| `string` + `password`                      | `P@ssw0rd1!`                            |
| `string` + `byte`                          | Base64-encoded string                   |
| `string` + name contains `city`            | City name from dataset                  |
| `string` + name contains `phone`/`tel`     | `+1-555-xxx-xxxx`                       |
| `integer` + name contains `age`            | 18–79                                   |
| `integer` + name contains `price`/`amount` | Multiple of 100                         |
| `number` + name contains `lat`             | Latitude                                |
| `number` + name contains `lon`/`lng`       | Longitude                               |
| `boolean`                                  | Alternates `true`/`false`               |
| `array`                                    | Generates inner items recursively       |
| `object`                                   | Generates nested properties recursively |
| `enum`                                     | Cycles through enum values              |

## Architecture

The project follows a layered architecture with clear separation of concerns:

```
ui/          → IntelliJ UI (dialog, table model, action)
application/ → Use case orchestration
domain/      → Business logic (example generation, domain models)
infrastructure/ → External concerns (OpenAPI spec parsing)
```

- **Domain models** are Java records: `SchemaDefinition`, `PropertyDefinition`, `GeneratedExample`, `ExampleGenerationRequest`.
- **Domain & application services** use Lombok where applicable.
- **Infrastructure** uses [Swagger Parser](https://github.com/swagger-api/swagger-parser) for spec parsing.
- Built with the [IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/) v2.2.1.

## Build

```bash
./gradlew buildPlugin
```

Run tests:

```bash
./gradlew test
```

## License

Copyright (c) 2026 Luis Paolo Pepe Barra (@LuisPPB16). All rights reserved.
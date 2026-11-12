# Graph Report - OpenAPI-Generator  (2026-11-12)

## Corpus Check
- Corpus is ~47,302 words - fits in a single context window. You may not need a graph.

## Summary
- 596 nodes · 1494 edges · 29 communities (13 shown, 16 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 155 edges (avg confidence: 0.85)
- Token cost: 352,500 input · 0 output

## Community Hubs (Navigation)
- Domain Model Tests
- IntelliJ Platform & Swing APIs
- Schema Count Table Model
- Plugin Settings UI
- CI Build Workflow
- README Feature Overview
- Release Workflow
- OpenAPI Spec Parser
- Example Generation Domain
- Parser Test Coverage
- File Writer & JSON Nodes
- Domain Model Records
- Gradle CI Workflow
- Application Service
- Validate Spec Action
- Output Format & Mapping
- Spec Validator
- Generate Examples Action
- Service Collaborators
- Action Context & Files
- Plugin Icon Assets
- Parser Locations

## God Nodes (most connected - your core abstractions)
1. `ExampleGenerationDomainServiceTest` - 76 edges
2. `GenerateExamplesDialog` - 48 edges
3. `PropertyDefinition` - 28 edges
4. `SchemaCountTableModelTest` - 28 edges
5. `ExampleGenerationDomainService` - 27 edges
6. `SchemaDefinition` - 25 edges
7. `OpenApiSpecificationParserTest` - 22 edges
8. `OpenApiSpecificationParser` - 19 edges
9. `GeneratedExample` - 18 edges
10. `ExampleGenerationRequest` - 17 edges

## Surprising Connections (you probably didn't know these)
- `IntelliJ Plugin Icon (Light Theme, SVG Vector)` --semantically_similar_to--> `IntelliJ Plugin Icon (Light Theme, PNG)`  [INFERRED] [semantically similar]
  src/main/resources/META-INF/pluginIcon.svg → src/main/resources/META-INF/pluginIcon.png
- `Plugin Logo Master Artwork with Transparency (PNG)` --semantically_similar_to--> `IntelliJ Plugin Icon (Light Theme, PNG)`  [INFERRED] [semantically similar]
  src/main/resources/pluginIcon-.png → src/main/resources/META-INF/pluginIcon.png
- `Plugin Logo Master Artwork on White (JPG)` --semantically_similar_to--> `IntelliJ Plugin Icon (Light Theme, PNG)`  [INFERRED] [semantically similar]
  src/main/resources/pluginIcon-.jpg → src/main/resources/META-INF/pluginIcon.png
- `IntelliJ Plugin Icon (Light Theme, SVG Vector)` --semantically_similar_to--> `Plugin Logo Master Artwork on White (JPG)`  [INFERRED] [semantically similar]
  src/main/resources/META-INF/pluginIcon.svg → src/main/resources/pluginIcon-.jpg
- `Plugin Logo Master Artwork with Transparency (PNG)` --semantically_similar_to--> `Plugin Logo Master Artwork on White (JPG)`  [INFERRED] [semantically similar]
  src/main/resources/pluginIcon-.png → src/main/resources/pluginIcon-.jpg

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Architectural layers of OpenAPI SpecTacular** — readme_ui_layer, readme_application_layer, readme_domain_layer, readme_infrastructure_layer [EXTRACTED 1.00]
- **Domain model records** — readme_schema_definition, readme_property_definition, readme_generated_example, readme_example_generation_request [EXTRACTED 1.00]
- **Code quality and build toolchain** — readme_pmd, readme_spotless_google_java_format, readme_plugin_verifier [EXTRACTED 1.00]
- **IntelliJ Plugin Icon Asset Family** — src_main_resources_meta_inf_pluginicon_plugin_icon_light_png, src_main_resources_meta_inf_pluginicon_plugin_icon_light_svg, src_main_resources_pluginicon__master_logo_transparent_png, src_main_resources_pluginicon__master_logo_white_jpg [INFERRED 0.85]

## Communities (29 total, 16 thin omitted)

### Community 0 - "Domain Model Tests"
Cohesion: 0.08
Nodes (3): ExampleGenerationRequestTest, PropertyDefinitionTest, ExampleGenerationDomainServiceTest

### Community 1 - "IntelliJ Platform & Swing APIs"
Cohesion: 0.05
Nodes (9): GenerateExamplesDialog, DocumentListener, FocusAdapter, Task.Backgroundable, Task.Backgroundable, Task.Backgroundable, SpinnerCellEditor, EditorDelegate (+1 more)

### Community 3 - "Plugin Settings UI"
Cohesion: 0.10
Nodes (4): ExampleGeneratorConfigurable, ExampleGeneratorSettings, State, ExampleGeneratorSettingsTest

### Community 4 - "CI Build Workflow"
Cohesion: 0.07
Nodes (32): name, runs-on, steps, concurrency, cancel-in-progress, group, GRADLE_OPTS, jobs (+24 more)

### Community 5 - "README Feature Overview"
Cohesion: 0.08
Nodes (30): Application layer, Automatic spec parsing, Changelog 1.1.2, Combined output mode (all-examples.json), Custom example values, Domain layer, Enum support, ExampleGenerationRequest (+22 more)

### Community 6 - "Release Workflow"
Cohesion: 0.07
Nodes (28): CERTIFICATE_CHAIN, PRIVATE_KEY, PRIVATE_KEY_PASSWORD, PUBLISH_TOKEN, jobs, release, name, on (+20 more)

### Community 9 - "Example Generation Domain"
Cohesion: 0.28
Nodes (3): PropertyDefinition, ExampleGenerationDomainService, GenerationContext

### Community 11 - "File Writer & JSON Nodes"
Cohesion: 0.23
Nodes (3): ExampleFileWriter, WrittenFile, ExampleFileWriterTest

### Community 13 - "Gradle CI Workflow"
Cohesion: 0.12
Nodes (16): permissions, runs-on, steps, jobs, build, name, on, workflow_dispatch (+8 more)

### Community 14 - "Application Service"
Cohesion: 0.34
Nodes (4): ExampleGenerationApplicationService, GenerationSummary, ExampleGenerationRequest, ExampleGenerationApplicationServiceTest

### Community 18 - "Output Format & Mapping"
Cohesion: 0.18
Nodes (3): OutputFormat, JSON, YAML

### Community 19 - "Spec Validator"
Cohesion: 0.42
Nodes (3): OpenApiSpecValidator, ValidationResult, OpenApiSpecValidatorTest

### Community 25 - "Plugin Icon Assets"
Cohesion: 0.83
Nodes (4): IntelliJ Plugin Icon (Light Theme, PNG), IntelliJ Plugin Icon (Light Theme, SVG Vector), Plugin Logo Master Artwork with Transparency (PNG), Plugin Logo Master Artwork on White (JPG)

## Knowledge Gaps
- **11 isolated node(s):** `JSON`, `YAML`, `Enum support`, `Custom example values`, `Nested objects and arrays generation` (+6 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 122 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **16 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GenerateExamplesDialog` connect `IntelliJ Platform & Swing APIs` to `Schema Count Table Model`, `Plugin Settings UI`, `IntelliJ Platform Utilities`, `Domain Model Records`, `Application Service`, `Validate Spec Action`, `Output Format & Mapping`, `Generate Examples Action`, `Action Context & Files`?**
  _High betweenness centrality (0.137) - this node is a cross-community bridge._
- **What connects `JSON`, `YAML`, `Enum support` to the rest of the system?**
  _11 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Domain Model Tests` be split into smaller, more focused modules?**
  _Cohesion score 0.07797537619699042 - nodes in this community are weakly interconnected._
- **Why does `SchemaDefinition` connect `Domain Model Records` to `Domain Model Tests`, `IntelliJ Platform & Swing APIs`, `Schema Count Table Model`, `OpenAPI Spec Parser`, `IntelliJ Platform Utilities`, `Example Generation Domain`, `Parser Test Coverage`, `Application Service`, `Java Standard Library`, `File IO & Jackson APIs`, `Service Collaborators`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Should `IntelliJ Platform & Swing APIs` be split into smaller, more focused modules?**
  _Cohesion score 0.0546583850931677 - nodes in this community are weakly interconnected._
- **Why does `ExampleGenerationDomainService` connect `Example Generation Domain` to `Domain Model Tests`, `IntelliJ Platform Utilities`, `Application Service`, `Java Standard Library`, `File IO & Jackson APIs`, `Validate Spec Action`, `Output Format & Mapping`, `Service Collaborators`, `Test Fixtures`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **Should `Schema Count Table Model` be split into smaller, more focused modules?**
  _Cohesion score 0.14615384615384616 - nodes in this community are weakly interconnected._
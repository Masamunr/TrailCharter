#!/usr/bin/env python3
"""Exercise the additive Room 2→3 migration against representative v2 data."""

from __future__ import annotations

import json
from pathlib import Path
import sqlite3
import unittest


ROOT = Path(__file__).resolve().parents[2]
SCHEMA_2 = ROOT / "app/schemas/com.masamunr.trailcharter.data.adventure.TrailCharterDatabase/2.json"
SCHEMA_3 = ROOT / "app/schemas/com.masamunr.trailcharter.data.adventure.TrailCharterDatabase/3.json"

MIGRATION_2_3 = (
    """CREATE TABLE IF NOT EXISTS `stage_routes` (
        `stageId` INTEGER NOT NULL, `planningMode` TEXT NOT NULL, `travelMode` TEXT NOT NULL,
        `snapToNetwork` INTEGER NOT NULL, `distanceMetres` REAL, `ascentMetres` REAL,
        `descentMetres` REAL, `durationSeconds` INTEGER, `createdAtEpochMillis` INTEGER NOT NULL,
        `updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`stageId`),
        FOREIGN KEY(`stageId`) REFERENCES `stages`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""",
    """CREATE TABLE IF NOT EXISTS `stage_route_control_points` (
        `stageId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `role` TEXT NOT NULL,
        `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, PRIMARY KEY(`stageId`, `position`),
        FOREIGN KEY(`stageId`) REFERENCES `stage_routes`(`stageId`) ON UPDATE NO ACTION ON DELETE CASCADE)""",
    """CREATE TABLE IF NOT EXISTS `stage_route_geometry_points` (
        `stageId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `latitude` REAL NOT NULL,
        `longitude` REAL NOT NULL, PRIMARY KEY(`stageId`, `position`),
        FOREIGN KEY(`stageId`) REFERENCES `stage_routes`(`stageId`) ON UPDATE NO ACTION ON DELETE CASCADE)""",
)


class Migration2To3Test(unittest.TestCase):
    def test_v3_export_preserves_every_v2_entity_definition(self) -> None:
        schema_2 = json.loads(SCHEMA_2.read_text(encoding="utf-8"))["database"]
        schema_3 = json.loads(SCHEMA_3.read_text(encoding="utf-8"))["database"]
        v2_entities = {entity["tableName"]: entity for entity in schema_2["entities"]}
        v3_entities = {entity["tableName"]: entity for entity in schema_3["entities"]}

        self.assertEqual(2, schema_2["version"])
        self.assertEqual(3, schema_3["version"])
        for table_name, entity in v2_entities.items():
            self.assertEqual(entity, v3_entities[table_name], f"v2 table changed during migration: {table_name}")

    def test_existing_adventure_stage_and_itinerary_survive(self) -> None:
        schema = json.loads(SCHEMA_2.read_text(encoding="utf-8"))["database"]
        database = sqlite3.connect(":memory:")
        database.execute("PRAGMA foreign_keys = ON")
        for entity in schema["entities"]:
            database.execute(entity["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
            for index in entity.get("indices", []):
                database.execute(index["createSql"].replace("${TABLE_NAME}", entity["tableName"]))

        database.execute(
            "INSERT INTO adventures VALUES (7, 'Existing adventure', 'Keep me', 20000, 20002, 100, 200)"
        )
        database.execute("INSERT INTO stages VALUES (11, 7, 'Existing stage', 0, 1, 1234)")
        database.execute(
            "INSERT INTO itinerary_items VALUES (13, 7, 11, 'Existing item', 'Keep this too', 0, 0, NULL)"
        )
        for statement in MIGRATION_2_3:
            database.execute(statement)

        self.assertEqual(
            (7, "Existing adventure", "Keep me"),
            database.execute("SELECT id, title, summary FROM adventures").fetchone(),
        )
        self.assertEqual(
            (11, 7, "Existing stage", 1),
            database.execute("SELECT id, adventureId, title, isComplete FROM stages").fetchone(),
        )
        self.assertEqual(
            (13, 11, "Existing item", "Keep this too"),
            database.execute("SELECT id, stageId, title, note FROM itinerary_items").fetchone(),
        )
        self.assertEqual(
            {"stage_routes", "stage_route_control_points", "stage_route_geometry_points"},
            {
                row[0]
                for row in database.execute(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'stage_route%'"
                )
            },
        )

        database.execute(
            "INSERT INTO stage_routes VALUES (11, 'MAGNETIC', 'WALK', 1, 1000, 10, 5, 900, 300, 300)"
        )
        database.execute("INSERT INTO stage_route_control_points VALUES (11, 0, 'START', 53.1, -4.1)")
        database.execute("INSERT INTO stage_route_control_points VALUES (11, 1, 'FINISH', 53.2, -4.2)")
        database.execute("DELETE FROM stages WHERE id = 11")
        self.assertEqual(0, database.execute("SELECT COUNT(*) FROM stage_routes").fetchone()[0])
        self.assertEqual(0, database.execute("SELECT COUNT(*) FROM stage_route_control_points").fetchone()[0])


if __name__ == "__main__":
    unittest.main()

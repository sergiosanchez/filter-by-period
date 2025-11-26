# Liferay DXP Content Web (JournalArticle) Date Filter (Period-Based)

This module provides a custom **Info Collection Provider** and **Info Filter** implementation for Liferay DXP. It allows users to filter collections of **Web Content (JournalArticle)** on a Content Page by pre-defined relative date ranges (e.g., Last Month, Last Year), instead of a single specific date.

This solution ensures that complex date calculation logic remains on the backend (Java), enhancing filtering accuracy, especially with strict Elasticsearch date mappings.

---

## 🚀 Key Features

* **Targeted Filtering:** Specifically filters assets of type **`JournalArticle`** (Web Content).
* **Period-Based Logic:** Filters content based on relative periods (`now-1M`, `now-1y`).
* **Backend Calculation:** Uses standard Java Calendar utilities to dynamically calculate the `startDate` and `endDate` for the Elasticsearch **`RangeTermFilter`**.
* **Frontend Integration:** Provides a simple `<select>` (combobox) UI interface.

---

## ⚙️ Core Components and Classes

The solution is comprised of three main Java classes and two frontend files:

### 1. Java Backend (OSGi Modules)

| Class | Type | Description |
| :--- | :--- | :--- |
| `NoticiasCollectionProvider.java` | **InfoCollectionProvider** | This is the main entry point. It handles the core logic: It registers the custom filter, and its internal method (`_getDateBooleanClause`) calculates the dynamic date range based on the `periodKey` received. It specifically targets the `Field.CREATE_DATE` (or `Field.MODIFIED_DATE`) of `JournalArticle` assets. |
| `DateInfoFilter.java` | **InfoFilter** | Defines the data structure for the filter value. It holds the selected period key as a **String** (`_periodKey`)—e.g., `"lastYear"`—after removing the methods related to the deprecated `java.util.Date` object. |
| `DateInfoFilterProvider.java` | **InfoFilterProvider** | Handles the marshaling/unmarshaling of the filter value. It reads the raw **String value** (the period key) sent by the frontend and sets it on the `DateInfoFilter` object using the `setPeriodKey(String)` method. |

### 2. Frontend (Fragment/Client Extension)

| File | Type | Description |
| :--- | :--- | :--- |
| `page.jsp` | **Fragment View** | Defines the structure of the filter interface. It contains a standard HTML **`<select>` combobox** whose `value` attributes correspond to the `periodKey` strings used in the Java backend (e.g., `"lastMonth"`). |
| `DateFilter.js` | **JavaScript Component** | This module handles the user interaction. It listens for changes in the combobox and uses the Liferay frontend API (`setCollectionFilterValue`) to send the selected **`periodKey`** (String) to the backend via the custom `date` filter key. |

---

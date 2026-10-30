# PrintQueue – Smart Campus Printing Queue System

A Java-based console application that manages campus printing requests using FIFO queue management, multithreading, file handling, and object-oriented programming.

## Overview
PrintQueue is a Smart Campus Printing Queue System designed to streamline document printing in college printing centers, eliminating long waiting times and preventing order mix-ups.

### Key Highlights
- **User Roles:** Student and Administrator
- **Queue Management:** FIFO (First In, First Out) ordering via Java `Queue` & `LinkedList`
- **Simulated Printing:** Background processing with Java Multithreading (`Thread` / `Runnable`) and animated visual progress bar
- **Printer Management:** Real-time printer status monitor (`AVAILABLE`, `BUSY`, `OFFLINE`)
- **Data Persistence:** Local flat-file storage (`data/students.txt`, `data/orders.txt`, `data/printers.txt`)
- **Rich Terminal Interface:** ASCII artwork and ANSI colored dashboards

## Default Admin Credentials
- **Username:** `admin`
- **Password:** `admin123`

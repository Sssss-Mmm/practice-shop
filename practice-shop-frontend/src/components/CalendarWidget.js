import React, { useState, useMemo, useEffect } from 'react';
import './CalendarWidget.css';

const CalendarWidget = ({ availableDates = [], selectedDate, onDateSelect }) => {
    // availableDates format: ['2025-12-10', '2025-12-11', ...]
    
    // Parse the initial month from selectedDate or availableDates[0] or current date
    const initialDate = useMemo(() => {
        if (selectedDate) return new Date(selectedDate);
        if (availableDates.length > 0) return new Date(availableDates[0]);
        return new Date();
    }, [selectedDate, availableDates]);

    const [currentMonth, setCurrentMonth] = useState(new Date(initialDate.getFullYear(), initialDate.getMonth(), 1));

    // Ensure state updates if props change significantly (optional, but good for UX)
    useEffect(() => {
        if (selectedDate) {
            const d = new Date(selectedDate);
            setCurrentMonth(new Date(d.getFullYear(), d.getMonth(), 1));
        }
    }, [selectedDate]);

    const daysInMonth = (date) => {
        return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
    };

    const firstDayOfMonth = (date) => {
        return new Date(date.getFullYear(), date.getMonth(), 1).getDay();
    };

    const handlePrevMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1));
    };

    const handleNextMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1));
    };

    const handleDateClick = (day) => {
        const dateStr = `${currentMonth.getFullYear()}-${String(currentMonth.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        if (availableDates.includes(dateStr)) {
            onDateSelect(dateStr);
        }
    };

    const renderDays = () => {
        const days = [];
        const totalDays = daysInMonth(currentMonth);
        const startDay = firstDayOfMonth(currentMonth);

        // Empty cells for offset
        for (let i = 0; i < startDay; i++) {
            days.push(<div key={`empty-${i}`} className="day-cell empty"></div>);
        }

        // Day cells
        for (let day = 1; day <= totalDays; day++) {
            const dateStr = `${currentMonth.getFullYear()}-${String(currentMonth.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const isAvailable = availableDates.includes(dateStr);
            const isSelected = selectedDate === dateStr;

            days.push(
                <div 
                    key={day} 
                    className={`day-cell ${isAvailable ? 'available' : ''} ${isSelected ? 'selected' : ''}`}
                    onClick={() => isAvailable && handleDateClick(day)}
                >
                    {day}
                    {isAvailable && <span className="dot-marker"></span>}
                </div>
            );
        }
        return days;
    };

    const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];

    return (
        <div className="calendar-widget">
            <div className="calendar-header">
                <button className="nav-btn" onClick={handlePrevMonth}>&lt;</button>
                <div className="month-label">
                    {currentMonth.getFullYear()}년 {monthNames[currentMonth.getMonth()]}
                </div>
                <button className="nav-btn" onClick={handleNextMonth}>&gt;</button>
            </div>
            <div className="calendar-grid">
                {['일', '월', '화', '수', '목', '금', '토'].map(d => (
                    <div key={d} className="day-name">{d}</div>
                ))}
                {renderDays()}
            </div>
        </div>
    );
};

export default CalendarWidget;

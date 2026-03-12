import { describe, it, expect } from 'vitest';

import { TicketStatusService } from './ticket-status.service';

describe('TicketStatusService', () => {
  const service = new TicketStatusService();

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getDisplay', () => {
    it('should return label key and css class for OPEN', () => {
      const display = service.getDisplay('OPEN');
      expect(display.labelKey).toBe('client.ticketStatus.open');
      expect(display.cssClass).toBe('status-open');
    });

    it('should return label key and css class for IN_PROGRESS', () => {
      const display = service.getDisplay('IN_PROGRESS');
      expect(display.labelKey).toBe('client.ticketStatus.inProgress');
      expect(display.cssClass).toBe('status-in-progress');
    });

    it('should return label key and css class for RESOLVED', () => {
      const display = service.getDisplay('RESOLVED');
      expect(display.labelKey).toBe('client.ticketStatus.resolved');
      expect(display.cssClass).toBe('status-resolved');
    });

    it('should be case insensitive', () => {
      const display = service.getDisplay('open');
      expect(display.labelKey).toBe('client.ticketStatus.open');
    });

    it('should return unknown for null or undefined', () => {
      expect(service.getDisplay(null).labelKey).toBe('client.ticketStatus.unknown');
      expect(service.getDisplay(undefined).labelKey).toBe('client.ticketStatus.unknown');
    });

    it('should return unknown for unrecognized status', () => {
      const display = service.getDisplay('CUSTOM');
      expect(display.labelKey).toBe('client.ticketStatus.unknown');
      expect(display.cssClass).toBe('status-unknown');
    });
  });

  describe('isOpenStatus', () => {
    it('should return true for OPEN, IN_PROGRESS, AWAITING_VALIDATION', () => {
      expect(service.isOpenStatus('OPEN')).toBe(true);
      expect(service.isOpenStatus('IN_PROGRESS')).toBe(true);
      expect(service.isOpenStatus('AWAITING_VALIDATION')).toBe(true);
    });

    it('should return false for RESOLVED and CLOSED', () => {
      expect(service.isOpenStatus('RESOLVED')).toBe(false);
      expect(service.isOpenStatus('CLOSED')).toBe(false);
    });

    it('should return false for null or empty', () => {
      expect(service.isOpenStatus(null)).toBe(false);
      expect(service.isOpenStatus('')).toBe(false);
    });
  });
});

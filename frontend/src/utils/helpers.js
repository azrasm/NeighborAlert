import { CATEGORY_LABELS, STATUS_LABELS, STATUS_CHIP_MAP } from './constants';

export const getCategoryName = (report) => {
  if (report.category?.name) return report.category.name;
  const catId = report.categoryId || report.category?.id;
  if (catId) return CATEGORY_LABELS[Number(catId)] || `Kat. ${catId}`;
  return "Ostalo";
};

export const getStatusName = (report) => {
  if (report.status?.name) return report.status.name;
  const statId = report.statusId || report.status?.id;
  if (statId) return STATUS_LABELS[Number(statId)] || `Status ${statId}`;
  return "Prijavljeno";
};

export const getStatusId = (report) => report.status?.id || report.statusId || 1;

export const getStatusChip = (report) => STATUS_CHIP_MAP[getStatusName(report)] || "chip-category";

export const parseJwt = (token) => {
  try { 
    return JSON.parse(atob(token.split('.')[1])); 
  } catch { 
    return null; 
  }
};

export const fileToDataUrl = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = e => resolve(e.target.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
};
export const CATEGORY_LABELS = { 
  1: "Oštećenje puta", 
  2: "Sigurnost", 
  3: "Komunalne usluge", 
  4: "Okoliš", 
  5: "Ostalo" 
};

export const STATUS_LABELS = { 
  1: "Prijavljeno", 
  2: "U toku", 
  3: "Riješeno", 
  4: "Odbijeno" 
};

export const STATUS_CHIP_MAP = { 
  "Prijavljeno": "chip-pending", 
  "U toku": "chip-progress", 
  "Riješeno": "chip-resolved", 
  "Odbijeno": "chip-pending" 
};

export const STATUS_COLORS = { 
  "Prijavljeno": "#ffa502", 
  "U toku": "#5bc8ff", 
  "Riješeno": "#2ed573", 
  "Odbijeno": "#ff4757" 
};

export const FLAG_REASONS = [
  "Lažna prijava - izmišljeni incident",
  "Neprikladni sadržaj",
  "Spam ili duplicirana prijava",
  "Netačna lokacija",
  "Uvredljiv ili neprimjeren opis",
  "Ostalo",
];
const express = require('express');
const multer = require('multer');
const path = require('path');
const cors = require('cors');
const fs = require('fs');
const { Sequelize, DataTypes } = require('sequelize');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

const UPLOAD_DIR = path.join(__dirname, 'uploads');
if (!fs.existsSync(UPLOAD_DIR)) fs.mkdirSync(UPLOAD_DIR, { recursive: true });
const JWT_SECRET = process.env.JWT_SECRET || 'change_me';
const PORT = process.env.PORT || 4000;
const BASE_URL = process.env.BASE_URL || `http://localhost:${PORT}`;

// Configura Sequelize (Postgres)
const sequelize = new Sequelize(process.env.DATABASE_URL || 'postgres://postgres:postgres@db:5432/reportsdb', {
  dialect: 'postgres',
  logging: false
});

// Modelos
const User = sequelize.define('User', {
  email: { type: DataTypes.STRING, unique: true, allowNull: false },
  password_hash: { type: DataTypes.STRING, allowNull: false }
});

const Report = sequelize.define('Report', {
  title: { type: DataTypes.STRING, allowNull: false },
  description: { type: DataTypes.TEXT },
  latitude: { type: DataTypes.DOUBLE, allowNull: false },
  longitude: { type: DataTypes.DOUBLE, allowNull: false },
  image_url: { type: DataTypes.STRING }
});

User.hasMany(Report);
Report.belongsTo(User);

// Multer para subir archivos
const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, UPLOAD_DIR),
  filename: (req, file, cb) => {
    const unique = Date.now() + '-' + Math.round(Math.random()*1e6);
    cb(null, unique + path.extname(file.originalname));
  }
});
const upload = multer({ storage });

const app = express();
app.use(cors());
app.use(express.json());
app.use('/uploads', express.static(UPLOAD_DIR));

// Autenticación simple
function authMiddleware(req, res, next) {
  const header = req.headers.authorization;
  if (!header) return res.status(401).json({ error: 'Missing token' });
  const token = header.replace('Bearer ', '');
  try {
    const payload = jwt.verify(token, JWT_SECRET);
    req.userId = payload.userId;
    next();
  } catch (e) {
    res.status(401).json({ error: 'Invalid token' });
  }
}

// Auth routes
app.post('/auth/register', async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) return res.status(400).json({ error: 'Missing email or password' });
  const hash = await bcrypt.hash(password, 10);
  try {
    const user = await User.create({ email, password_hash: hash });
    res.json({ id: user.id, email: user.email });
  } catch (e) {
    res.status(400).json({ error: e.message });
  }
});

app.post('/auth/login', async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) return res.status(400).json({ error: 'Missing email or password' });
  const user = await User.findOne({ where: { email }});
  if (!user) return res.status(401).json({ error: 'Invalid credentials' });
  const ok = await bcrypt.compare(password, user.password_hash);
  if (!ok) return res.status(401).json({ error: 'Invalid credentials' });
  const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '7d' });
  res.json({ token });
});

// Reports
app.get('/reports', async (req, res) => {
  const reports = await Report.findAll({ order: [['createdAt','DESC']]});
  res.json(reports);
});

app.get('/reports/:id', async (req, res) => {
  const r = await Report.findByPk(req.params.id);
  if (!r) return res.status(404).json({ error: 'Not found' });
  res.json(r);
});

app.post('/reports', authMiddleware, upload.single('image'), async (req, res) => {
  const { title, description, latitude, longitude } = req.body;
  const imageFile = req.file;
  const imageUrl = imageFile ? `${BASE_URL}/uploads/${imageFile.filename}` : null;
  try {
    const report = await Report.create({
      userId: req.userId,
      title,
      description,
      latitude: parseFloat(latitude),
      longitude: parseFloat(longitude),
      image_url: imageUrl
    });
    res.json(report);
  } catch (e) {
    res.status(400).json({ error: e.message });
  }
});

// Inicializar DB y escuchar
(async () => {
  try {
    await sequelize.sync({ alter: true }); // para desarrollo. En producción usar migraciones
    app.listen(PORT, () => console.log(`Server running on ${BASE_URL}`));
  } catch (e) {
    console.error(e);
  }
})();


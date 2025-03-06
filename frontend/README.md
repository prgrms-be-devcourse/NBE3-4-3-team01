```
frontend
├─ .gitignore
├─ components.json
├─ eslint.config.mjs
├─ next.config.ts
├─ package-lock.json
├─ package.json
├─ postcss.config.mjs
├─ public
│  ├─ file.svg
│  ├─ globe.svg
│  ├─ next.svg
│  ├─ vercel.svg
│  └─ window.svg
├─ README.md
├─ src
│  ├─ app
│  │  ├─ admin
│  │  │  ├─ business
│  │  │  │  └─ page.tsx
│  │  │  ├─ hotel-options
│  │  │  │  └─ page.tsx
│  │  │  ├─ hotels
│  │  │  │  └─ page.tsx
│  │  │  └─ room-options
│  │  │     └─ page.tsx
│  │  ├─ business
│  │  │  ├─ bookings
│  │  │  │  └─ page.tsx
│  │  │  ├─ hotels
│  │  │  │  ├─ page.tsx
│  │  │  │  └─ [hotelId]
│  │  │  │     └─ reviews
│  │  │  │        └─ page.tsx
│  │  │  ├─ register
│  │  │  │  └─ page.tsx
│  │  │  ├─ revenue
│  │  │  │  └─ page.tsx
│  │  │  └─ rooms
│  │  │     └─ [roomId]
│  │  │        └─ page.tsx
│  │  ├─ favicon.ico
│  │  ├─ globals.css
│  │  ├─ hotels
│  │  │  ├─ page.tsx
│  │  │  └─ [hotelId]
│  │  │     ├─ page.tsx
│  │  │     └─ reviews
│  │  │        └─ page.tsx
│  │  ├─ join
│  │  │  └─ page.tsx
│  │  ├─ layout.tsx
│  │  ├─ login
│  │  │  └─ page.tsx
│  │  ├─ me
│  │  │  ├─ favorites
│  │  │  │  └─ page.tsx
│  │  │  ├─ orders
│  │  │  │  ├─ page.tsx
│  │  │  │  └─ [bookingId]
│  │  │  │     └─ page.tsx
│  │  │  └─ reviews
│  │  │     ├─ page.tsx
│  │  │     └─ [reviewId]
│  │  │        └─ page.tsx
│  │  ├─ orders
│  │  │  └─ payment
│  │  │     └─ page.tsx
│  │  └─ page.tsx
│  ├─ components
│  │  ├─ Pagination
│  │  │  ├─ Pagination.module.css
│  │  │  └─ Pagination.tsx
│  │  ├─ ReviewWithComment
│  │  │  ├─ HotelReviews.tsx
│  │  │  ├─ HotelReviewWithComment.tsx
│  │  │  ├─ MyReviews.tsx
│  │  │  ├─ MyReviewWithComment.tsx
│  │  │  └─ ReviewList.tsx
│  │  └─ ui
│  │     ├─ button.tsx
│  │     ├─ card.tsx
│  │     ├─ input.tsx
│  │     ├─ label.tsx
│  │     └─ textarea.tsx
│  └─ lib
│     ├─ api
│     │  ├─ AwsS3Api.ts
│     │  ├─ ReviewApi.ts
│     │  └─ ReviewCommentApi.ts
│     ├─ types
│     │  ├─ Empty.ts
│     │  ├─ GetReviewResponse.ts
│     │  ├─ HotelReviewListResponse.ts
│     │  ├─ HotelReviewResponse.ts
│     │  ├─ HotelReviewWithCommentDto.ts
│     │  ├─ MyReviewResponse.ts
│     │  ├─ MyReviewWithCommentDto.ts
│     │  ├─ PageDto.ts
│     │  ├─ PostReviewRequest.ts
│     │  ├─ PresignedUrlsResponse.ts
│     │  ├─ ReviewCommentDto.ts
│     │  ├─ ReviewDto.ts
│     │  ├─ RsData.ts
│     │  └─ UpdateReviewRequest.ts
│     └─ utils.ts
├─ tailwind.config.ts
└─ tsconfig.json
This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
